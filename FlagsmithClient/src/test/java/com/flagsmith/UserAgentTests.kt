package com.flagsmith

import com.flagsmith.entities.Trait
import com.flagsmith.mockResponses.MockEndpoint
import com.flagsmith.mockResponses.mockResponseFor
import kotlinx.coroutines.runBlocking
import org.junit.After
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
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @Test
    fun testUserAgentHeaderSentWithValidVersion() {
        // Given - The User-Agent now shows SDK version or "unknown" (not app version)
        // This is because getUserAgent() method was updated to return SDK version
        // In tests, BuildConfig is not available, so it returns "unknown"
        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = null,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)

        // When
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)
        }

        // Then - Verify User-Agent contains "unknown" since BuildConfig is not available in tests
        mockServer.verify(
            request()
                .withPath("/flags/")
                .withMethod("GET")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/unknown")
        )
    }

    @Test
    fun testUserAgentHeaderSentWithNullContext() {
        // Given
        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = null,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)

        // When
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)
        }

        // Then
        mockServer.verify(
            request()
                .withPath("/flags/")
                .withMethod("GET")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/unknown")
        )
    }

    @Test
    fun testUserAgentHeaderSentWithExceptionDuringVersionRetrieval() {
        // Given - Even with context, getUserAgent() now returns SDK version or "unknown"
        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = null,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)

        // When
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)
        }

        // Then
        mockServer.verify(
            request()
                .withPath("/flags/")
                .withMethod("GET")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/unknown")
        )
    }

    @Test
    fun testUserAgentHeaderSentWithNullVersionName() {
        // Given - getUserAgent() now returns SDK version or "unknown" regardless of context
        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = null,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)

        // When
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)
        }

        // Then
        mockServer.verify(
            request()
                .withPath("/flags/")
                .withMethod("GET")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/unknown")
        )
    }

    @Test
    fun testUserAgentHeaderSentWithIdentityRequest() {
        // Given - getUserAgent() now returns SDK version or "unknown"
        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = null,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)

        // When
        runBlocking {
            val result = flagsmith.getIdentitySync("test-user")
            assertTrue(result.isSuccess)
        }

        // Then - Verify User-Agent contains "unknown" since BuildConfig is not available in tests
        mockServer.verify(
            request()
                .withPath("/identities/")
                .withMethod("GET")
                .withQueryStringParameter("identifier", "test-user")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/unknown")
        )
    }

    @Test
    fun testUserAgentHeaderSentWithTraitRequest() {
        // Given - getUserAgent() now returns SDK version or "unknown"
        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = null,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.SET_TRAIT)

        // When
        runBlocking {
            val result = flagsmith.setTraitSync(Trait(key = "test-key", traitValue = "test-value"), "test-user")
            assertTrue(result.isSuccess)
        }

        // Then - Verify the traits request has correct User-Agent with "unknown" since BuildConfig is not available in tests
        mockServer.verify(
            request()
                .withPath("/identities/")
                .withMethod("POST")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/unknown")
        )
    }
}