package com.flagsmith

import com.flagsmith.entities.Trait
import com.flagsmith.mockResponses.MockEndpoint
import com.flagsmith.mockResponses.MockResponses
import com.flagsmith.mockResponses.mockResponseFor
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json

class FeatureFlagTests {

    private lateinit var mockServer: ClientAndServer
    private lateinit var flagsmith: Flagsmith

    @Before
    fun setup() {
        mockServer = ClientAndServer.startClientAndServer()
        flagsmith = Flagsmith(
            environmentKey = "",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @Test
    fun testHasFeatureFlagWithFlag() {
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        runBlocking {
            val result = flagsmith.hasFeatureFlagSync("no-value")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow())
        }
    }

    @Test
    fun testHasFeatureFlagWithoutFlag() {
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        runBlocking {
            val result = flagsmith.hasFeatureFlagSync("doesnt-exist")
            assertTrue(result.isSuccess)
            assertFalse(result.getOrThrow())
        }
    }

    @Test
    fun testGetFeatureFlags() {
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            assertNotNull(found)
            assertEquals(7.0, found?.featureStateValue)
        }
    }

    @Test
    fun testGetFeatureFlagsWithIdentity() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync(identity = "person")
            assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            assertNotNull(found)
            assertEquals(756.0, found?.featureStateValue)
        }
    }

    @Test
    fun testGetValueForFeatureExisting() {
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("with-value", identity = null)
            assertTrue(result.isSuccess)
            assertEquals(7.0, result.getOrThrow())
        }
    }

    @Test
    fun testGetValueForFeatureExistingOverriddenWithIdentity() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("with-value", identity = "person")
            assertTrue(result.isSuccess)
            assertEquals(756.0, result.getOrThrow())
        }
    }

    @Test
    fun testGetValueForFeatureNotExisting() {
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("not-existing", identity = null)
            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }
    }

    @Test
    fun testHasFeatureForNoIdentity() {
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        runBlocking {
            val result =
                flagsmith.hasFeatureFlagSync("with-value-just-person-enabled", identity = null)
            assertTrue(result.isSuccess)
            assertFalse(result.getOrThrow())
        }
    }

    @Test
    fun testHasFeatureWithIdentity() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result =
                flagsmith.hasFeatureFlagSync("with-value-just-person-enabled", identity = "person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow())
        }
    }

    @Test
    fun testThrowsExceptionWhenCreatingAnalyticsWithoutAContext() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            flagsmith = Flagsmith(
                environmentKey = "",
                baseUrl = "http://localhost:${mockServer.localPort}",
                enableAnalytics = true
            )
        }
        assertEquals("Flagsmith requires a context to use the analytics feature", exception.message)
    }

    @Test
    fun testGetFeatureFlagsWithIdentityAndTraits() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync(identity = "person", traits = listOf())
            assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            assertNotNull(found)
            assertEquals(756.0, found?.featureStateValue)
        }
    }

    @Test
    fun testGetFeatureFlagsWithTransientTraits() {
        mockServer.`when`(
            request()
                .withPath("/identities/")
                .withMethod("POST")
                .withBody(
                    json(
                        """
                            {
                              "identifier": "identity",
                              "traits": [
                                {
                                  "trait_key": "transient-trait",
                                  "trait_value": "value",
                                  "transient": true
                                },
                                {
                                  "trait_key": "persisted-trait",
                                  "trait_value": "value",
                                  "transient": false
                                }
                              ],
                              "transient": false
                            }
                        """.trimIndent()
                    )
                )
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody(MockResponses.getTransientIdentities)
            )

        runBlocking {
            val transientTrait = Trait("transient-trait", "value", true)
            val persistedTrait = Trait("persisted-trait", "value", false)
            val result = flagsmith.getFeatureFlagsSync(
                "identity",
                listOf(transientTrait, persistedTrait),
                false,
            )

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testGetFeatureFlagsWithTransientIdentity() {
        mockServer.`when`(
            request()
                .withPath("/identities/")
                .withMethod("POST")
                .withBody(
                    json(
                        """
                            {
                              "identifier": "identity",
                              "traits": [],
                              "transient": true
                            }
                        """.trimIndent()
                    )
                )
        )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody(MockResponses.getTransientIdentities)
            )

        runBlocking {
            val result = flagsmith.getFeatureFlagsSync(
                "identity", listOf(),true,
            )
            assertTrue(result.isSuccess)
        }
    }
}