package com.flagsmith.internal

import com.google.gson.JsonParser
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpError.error
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.Delay
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class FlagsmithEventProcessorTest {

    private lateinit var mockServer: ClientAndServer

    /** Jitter that never waits, recording the bound of each backoff. */
    private val backoffBounds = mutableListOf<Long>()
    private val noJitter = object : Random() {
        override fun nextBits(bitCount: Int) = 0
        override fun nextLong(until: Long): Long {
            backoffBounds.add(until)
            return 0
        }
    }

    @Before
    fun setup() {
        mockServer = ClientAndServer.startClientAndServer()
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    private fun processor(
        maxBuffer: Int = FlagsmithEventProcessor.DEFAULT_MAX_BUFFER,
        requestTimeoutMillis: Long = 3000,
        flushPeriodSeconds: Int = 0
    ) =
        FlagsmithEventProcessor(
            environmentKey = "environment-key",
            eventsBaseUrl = "http://localhost:${mockServer.localPort}",
            flushPeriodSeconds = flushPeriodSeconds,
            maxBuffer = maxBuffer,
            requestTimeoutMillis = requestTimeoutMillis,
            random = noJitter
        )

    private fun respondWith(vararg statuses: Int) {
        statuses.forEach {
            mockServer.`when`(request().withPath("/v1/events"), Times.once()).respond(response().withStatusCode(it))
        }
        mockServer.`when`(request().withPath("/v1/events")).respond(response().withStatusCode(202))
    }

    /** The events of each request sent, in order. */
    private fun sentBatches() = mockServer.retrieveRecordedRequests(request().withPath("/v1/events")).map { request ->
        JsonParser.parseString(request.bodyAsString).asJsonObject["events"].asJsonArray
            .map { it.asJsonObject["event"].asString }
    }

    private fun FlagsmithEventProcessor.track(event: String) = trackEvent(event, "user_123", null, null, null)

    private fun FlagsmithEventProcessor.flushSync() {
        val done = CountDownLatch(1)
        flush { done.countDown() }
        assertTrue("Flush did not complete", done.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun testTransientFailuresAreRetriedWithBackoff() {
        // Given
        mockServer.`when`(request().withPath("/v1/events"), Times.once()).respond(response().withStatusCode(503))
        mockServer.`when`(request().withPath("/v1/events"), Times.once()).error(error().withDropConnection(true))
        mockServer.`when`(request().withPath("/v1/events")).respond(response().withStatusCode(202))
        val processor = processor()
        processor.track("checkout")

        // When
        processor.flushSync()

        // Then
        assertEquals(List(3) { listOf("checkout") }, sentBatches())
        assertEquals(listOf(1001L, 2001L), backoffBounds)
    }

    @Test
    fun testUnansweredRequestTimesOutAndIsRetried() {
        // Given
        mockServer.`when`(request().withPath("/v1/events"), Times.once())
            .respond(response().withStatusCode(202).withDelay(Delay.seconds(5)))
        mockServer.`when`(request().withPath("/v1/events")).respond(response().withStatusCode(202))
        val processor = processor(requestTimeoutMillis = 500)
        processor.track("checkout")

        // When
        val started = System.currentTimeMillis()
        processor.flushSync()

        // Then
        assertTrue(System.currentTimeMillis() - started < 4000)
        assertEquals(List(2) { listOf("checkout") }, sentBatches())
    }

    @Test
    fun testBatchThatKeepsFailingIsSentWithTheNextFlushAheadOfNewerEvents() {
        // Given
        respondWith(503, 503, 503)
        val processor = processor()
        processor.track("add_to_cart")
        processor.flushSync()

        // When
        processor.track("checkout")
        processor.flushSync()

        // Then
        assertEquals(List(3) { listOf("add_to_cart") } + listOf(listOf("add_to_cart", "checkout")), sentBatches())
    }

    @Test
    fun testKeptBatchIsSentWithTheNextFlushWhenNothingWasTrackedSince() {
        // Given
        respondWith(503, 503, 503)
        val processor = processor()
        processor.track("add_to_cart")
        processor.flushSync()

        // When
        processor.flushSync()

        // Then
        assertEquals(List(4) { listOf("add_to_cart") }, sentBatches())
    }

    @Test
    fun testBatchWhoseLastAttemptFailsOtherwiseIsKeptAfterServerErrors() {
        // Given
        respondWith(500, 500, 503)
        val processor = processor()
        processor.track("add_to_cart")
        processor.flushSync()

        // When
        processor.track("checkout")
        processor.flushSync()

        // Then
        assertEquals(listOf("add_to_cart", "checkout"), sentBatches().last())
    }

    @Test
    fun testBatchWhoseLastAttemptGetsAServerErrorIsDropped() {
        // Given
        respondWith(503, 503, 500)
        val processor = processor()
        processor.track("add_to_cart")
        processor.flushSync()

        // When
        processor.track("checkout")
        processor.flushSync()

        // Then
        assertEquals(List(3) { listOf("add_to_cart") } + listOf(listOf("checkout")), sentBatches())
    }

    @Test
    fun testBatchTheEventsApiRejectsIsDropped() {
        // Given
        respondWith(400)
        val processor = processor()
        processor.track("add_to_cart")
        processor.flushSync()

        // When
        processor.track("checkout")
        processor.flushSync()

        // Then
        assertEquals(listOf(listOf("add_to_cart"), listOf("checkout")), sentBatches())
    }

    @Test
    fun testRefusedEnvironmentKeyStopsEventTracking() {
        listOf(401, 403).forEach { status ->
            // Given
            mockServer.reset()
            respondWith(status)
            val processor = processor()
            processor.track("add_to_cart")
            processor.flushSync()

            // When
            processor.track("checkout")
            processor.flushSync()

            // Then
            assertEquals(listOf(listOf("add_to_cart")), sentBatches())
        }
    }

    @Test
    fun testFullBufferDropsItsOldestEvent() {
        // Given
        respondWith(503, 503, 503)
        val processor = processor(maxBuffer = 3)
        (1..3).forEach { processor.track("event_$it") }
        processor.flushSync()

        // When
        processor.track("event_4")
        processor.flushSync()

        // Then
        assertEquals(List(3) { listOf("event_1", "event_2", "event_3") } + listOf(listOf("event_2", "event_3", "event_4")), sentBatches())
    }

    @Test
    fun testFullBufferHoldingAKeptBatchWaitsForTheNextFlush() {
        // Given
        respondWith(503, 503, 503)
        val processor = processor(maxBuffer = 3, flushPeriodSeconds = 3600)
        (1..3).forEach { processor.track("event_$it") }
        processor.flushSync()

        // When
        (4..5).forEach { processor.track("event_$it") }
        processor.flushSync()

        // Then
        assertEquals(List(3) { listOf("event_1", "event_2", "event_3") } + listOf(listOf("event_3", "event_4", "event_5")), sentBatches())
    }

    @Test
    fun testFullBufferHoldingAKeptBatchIsSentWithoutAFlushTimer() {
        // Given
        respondWith(503, 503, 503)
        val processor = processor(maxBuffer = 3)
        (1..3).forEach { processor.track("event_$it") }
        processor.flushSync()

        // When
        processor.track("event_4")

        // Then
        val deadline = System.currentTimeMillis() + 5000
        while (sentBatches().size < 4 && System.currentTimeMillis() < deadline) Thread.sleep(50)
        assertEquals(List(3) { listOf("event_1", "event_2", "event_3") } + listOf(listOf("event_2", "event_3", "event_4")), sentBatches())
    }

    @Test
    fun testCloseSendsTheBufferedEventsRetriesIncluded() {
        // Given
        respondWith(503)
        val processor = processor()
        processor.track("checkout")

        // When
        processor.close(5000)

        // Then
        assertEquals(List(2) { listOf("checkout") }, sentBatches())
    }

    @Test
    fun testCloseReturnsWithinItsTimeoutWhenTheEventsApiDoesNotAnswer() {
        // Given
        mockServer.`when`(request().withPath("/v1/events"))
            .respond(response().withStatusCode(202).withDelay(Delay.seconds(10)))
        val processor = processor()
        processor.track("checkout")

        // When
        val started = System.currentTimeMillis()
        processor.close(1000)

        // Then
        assertTrue(System.currentTimeMillis() - started < 2000)
    }

    @Test
    fun testEventsTrackedAfterCloseAreIgnored() {
        // Given
        respondWith()
        val processor = processor()
        processor.close(1000)

        // When
        processor.track("checkout")
        processor.flushSync()

        // Then
        assertEquals(emptyList<List<String>>(), sentBatches())
    }
}
