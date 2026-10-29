package com.flagsmith

import com.flagsmith.entities.Trait
import com.flagsmith.mockResponses.MockResponses
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import org.mockserver.verify.VerificationTimes
import java.time.Duration

class EventsTests {

    private lateinit var mockServer: ClientAndServer
    private lateinit var flagsmith: Flagsmith

    @Before
    fun setup() {
        mockServer = ClientAndServer.startClientAndServer()
        flagsmith = Flagsmith(
            environmentKey = "environment-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false),
            enableEvents = true,
            eventsBaseUrl = "http://localhost:${mockServer.localPort}",
            eventsFlushPeriod = 0
        )
        mockServer.`when`(request().withPath("/v1/events"))
            .respond(response().withStatusCode(202))
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @Test
    fun testTrackedEventReachesFlagsmith() {
        // Given / When
        flagsmith.trackEvent(
            "checkout_completed",
            identity = "user_123",
            value = 42,
            metadata = mapOf("currency" to "GBP")
        )
        flagsmith.flushEvents()

        // Then
        await.atMost(Duration.ofSeconds(5)) untilAsserted {
            mockServer.verify(
                request()
                    .withPath("/v1/events")
                    .withMethod("POST")
                    .withHeader("X-Environment-Key", "environment-key")
                    .withBody(
                        json(
                            """
                                {
                                  "events": [
                                    {
                                      "event": "checkout_completed",
                                      "feature_name": null,
                                      "identifier": "user_123",
                                      "value": "42",
                                      "traits": null,
                                      "metadata": { "currency": "GBP" }
                                    }
                                  ]
                                }
                            """.trimIndent()
                        )
                    ),
                VerificationTimes.once()
            )
        }
    }

    @Test
    fun testIdentityInExperimentIsExposedToTheVariantItWasServed() {
        // Given
        mockServer.`when`(request().withPath("/identities/").withMethod("POST"))
            .respond(response().withStatusCode(200).withBody(MockResponses.postIdentityInExperiment))

        // When
        val flag = runBlocking {
            flagsmith.getExperimentFlagSync("banner", "user_123", listOf(Trait("plan", "premium")))
        }.getOrThrow()
        flagsmith.flushEvents()

        // Then
        assertEquals("hello treatment", flag?.featureStateValue)
        assertEquals("treatment", flag?.variant)
        await.atMost(Duration.ofSeconds(5)) untilAsserted {
            mockServer.verify(
                request()
                    .withPath("/v1/events")
                    .withBody(
                        json(
                            """
                                {
                                  "events": [
                                    {
                                      "event": "${'$'}flag_exposure",
                                      "feature_name": "banner",
                                      "identifier": "user_123",
                                      "value": "treatment",
                                      "traits": { "plan": "premium" },
                                      "metadata": { "experiment_id": 7 }
                                    }
                                  ]
                                }
                            """.trimIndent()
                        )
                    ),
                VerificationTimes.once()
            )
        }
    }

    @Test
    fun testIdentityOutsideExperimentIsNotExposed() {
        // Given
        mockServer.`when`(request().withPath("/identities/").withMethod("POST"))
            .respond(response().withStatusCode(200).withBody(MockResponses.postIdentityOutsideExperiment))

        // When
        val flag = runBlocking {
            flagsmith.getExperimentFlagSync("banner", "user_123", listOf(Trait("plan", "premium")))
        }.getOrThrow()
        flagsmith.flushEvents()

        // Then
        assertEquals("hello", flag?.featureStateValue)
        Thread.sleep(500)
        mockServer.verify(request().withPath("/v1/events"), VerificationTimes.never())
    }

    @Test
    fun testRepeatedExposuresAreSentOnce() {
        // Given / When
        repeat(2) {
            flagsmith.trackExposureEvent(
                "banner",
                identity = "user_123",
                value = "control",
                metadata = mapOf("experiment_id" to 1)
            )
        }
        flagsmith.flushEvents()

        // Then
        await.atMost(Duration.ofSeconds(5)) untilAsserted {
            val requests = mockServer.retrieveRecordedRequests(request().withPath("/v1/events"))
            assertEquals(1, requests.size)
            assertEquals(1, Regex("flag_exposure").findAll(requests[0].bodyAsString).count())
        }
    }

    @Test
    fun testEventsAreRetriedOnceWhenFlagsmithFailsToReceiveThem() {
        // Given
        mockServer.reset()
        mockServer.`when`(request().withPath("/v1/events"), Times.once())
            .respond(response().withStatusCode(500))
        mockServer.`when`(request().withPath("/v1/events"))
            .respond(response().withStatusCode(202))

        // When
        flagsmith.trackEvent("checkout_completed", identity = "user_123")
        flagsmith.flushEvents()

        // Then
        await.atMost(Duration.ofSeconds(10)) untilAsserted {
            mockServer.verify(
                request().withPath("/v1/events").withBody(json("""{ "events": [ { "event": "checkout_completed" } ] }""")),
                VerificationTimes.exactly(2)
            )
        }
    }

    @Test
    fun testEventNamesStartingWithDollarAreReserved() {
        // Given / When / Then
        assertThrows(IllegalArgumentException::class.java) {
            flagsmith.trackEvent("${'$'}flag_exposure", identity = "user_123")
        }
    }

    @Test
    fun testEventsAreNotSentUnlessEnabled() {
        // Given
        val flagsmithWithoutEvents = Flagsmith(
            environmentKey = "environment-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false),
            eventsBaseUrl = "http://localhost:${mockServer.localPort}"
        )

        // When
        flagsmithWithoutEvents.trackEvent("checkout_completed", identity = "user_123")
        flagsmithWithoutEvents.trackExposureEvent("banner", identity = "user_123", value = "control")
        flagsmithWithoutEvents.flushEvents()

        // Then
        Thread.sleep(500)
        mockServer.verify(request().withPath("/v1/events"), VerificationTimes.never())
    }
}
