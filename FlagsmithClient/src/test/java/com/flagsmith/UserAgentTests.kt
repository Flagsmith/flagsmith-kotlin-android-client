package com.flagsmith

import com.flagsmith.entities.Trait
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

    companion object {
        // Expected version when BuildConfig is not available (in tests)
        // This matches the hardcoded version in FlagsmithRetrofitService.getHardcodedVersion()
        // x-release-please-start-version
        private const val EXPECTED_FALLBACK_VERSION = "1.8.0"
        // x-release-please-end
        private const val EXPECTED_USER_AGENT = "flagsmith-kotlin-android-sdk/$EXPECTED_FALLBACK_VERSION"
    }

    @Before
    fun setup() {
        mockServer = ClientAndServer.startClientAndServer()
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @Test
    fun testUserAgentHeaderSentWithGetFlags() {
        // Given - BuildConfig is not available in tests, so falls back to hardcoded version (1.8.0)
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

        // Then - Verify User-Agent contains hardcoded version since BuildConfig is not available in tests
        mockServer.verify(
            request()
                .withPath("/flags/")
                .withMethod("GET")
                .withHeader("User-Agent", EXPECTED_USER_AGENT)
        )
    }

    @Test
    fun testUserAgentHeaderSentWithNullContext() {
        // Given - Context being null doesn't affect SDK version retrieval
        // BuildConfig lookup is independent of Android context
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

        // Then - Should still get hardcoded version
        mockServer.verify(
            request()
                .withPath("/flags/")
                .withMethod("GET")
                .withHeader("User-Agent", EXPECTED_USER_AGENT)
        )
    }

    @Test
    fun testUserAgentHeaderSentWithIdentityRequest() {
        // Given - Testing that User-Agent header is sent consistently across all API endpoints
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

        // Then - Verify User-Agent header is sent with GET /identities/
        mockServer.verify(
            request()
                .withPath("/identities/")
                .withMethod("GET")
                .withQueryStringParameter("identifier", "test-user")
                .withHeader("User-Agent", EXPECTED_USER_AGENT)
        )
    }

    @Test
    fun testUserAgentHeaderSentWithTraitRequest() {
        // Given - Testing that User-Agent header is sent with POST requests
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

        // Then - Verify User-Agent header is sent with POST /identities/
        mockServer.verify(
            request()
                .withPath("/identities/")
                .withMethod("POST")
                .withHeader("User-Agent", EXPECTED_USER_AGENT)
        )
    }

    @Test
    fun testUserAgentFormat() {
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
            flagsmith.getFeatureFlagsSync()
        }

        // Then - Verify User-Agent follows the format: flagsmith-kotlin-android-sdk/{version}
        val requests = mockServer.retrieveRecordedRequests(
            request().withPath("/flags/")
        )

        assertEquals(1, requests.size)
        val userAgentHeader = requests[0].getFirstHeader("User-Agent")

        // Verify format
        assertTrue("User-Agent should start with 'flagsmith-kotlin-android-sdk/'",
            userAgentHeader.startsWith("flagsmith-kotlin-android-sdk/"))

        // Verify version part exists and is not empty
        val version = userAgentHeader.substringAfter("flagsmith-kotlin-android-sdk/")
        assertTrue("Version should not be empty", version.isNotEmpty())

        // In test environment, should be the hardcoded fallback version
        assertEquals(EXPECTED_FALLBACK_VERSION, version)
    }
}
