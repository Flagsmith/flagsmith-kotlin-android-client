package com.flagsmith

import com.flagsmith.mockResponses.MockEndpoint
import com.flagsmith.mockResponses.mockResponseFor
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request

class UserAgentTests {

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
    fun testUserAgentHeaderIsSent() {
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)
        }

        val requests = mockServer.retrieveRecordedRequests(
            request()
                .withPath("/flags/")
                .withMethod("GET")
        )
        assertEquals(1, requests.size)

        val userAgent = requests[0].getFirstHeader("User-Agent")
        // x-release-please-start-version
        assertEquals("flagsmith-kotlin-android-sdk/1.9.0", userAgent)
        // x-release-please-end
    }
}
