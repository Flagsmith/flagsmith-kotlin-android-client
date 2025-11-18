package com.flagsmith.internal

import com.flagsmith.FlagsmithCacheConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SDK version retrieval functionality in FlagsmithRetrofitService.
 *
 * These tests verify that getSdkVersion() correctly returns the version set by release-please.
 */
class SdkVersionRetrievalTest {

    private lateinit var mockServer: MockWebServer

    companion object {
        // This should match the version in getSdkVersion() and in .release-please-manifest.json
        // x-release-please-start-version
        private const val EXPECTED_SDK_VERSION = "1.8.0"
        // x-release-please-end
        private const val USER_AGENT_PREFIX = "flagsmith-kotlin-android-sdk"
    }

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun testUserAgentInterceptorReturnsValidFormat() {
        // Given - Create a client with the user agent interceptor
        val interceptor = FlagsmithRetrofitService.userAgentInterceptor(null)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        // When - Make a request
        val request = Request.Builder()
            .url(mockServer.url("/"))
            .build()

        client.newCall(request).execute().use { response ->
            // Then - Verify the request was made with the correct User-Agent header
            val recordedRequest = mockServer.takeRequest()
            val userAgent = recordedRequest.getHeader("User-Agent")

            assertNotNull("User-Agent header should be present", userAgent)
            assertTrue(
                "User-Agent should start with correct prefix: $userAgent",
                userAgent!!.startsWith("$USER_AGENT_PREFIX/")
            )
        }
    }

    @Test
    fun testVersionFormatIsValid() {
        // Given - Create a client with the user agent interceptor
        val interceptor = FlagsmithRetrofitService.userAgentInterceptor(null)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        // When - Make a request
        val request = Request.Builder()
            .url(mockServer.url("/"))
            .build()

        client.newCall(request).execute().use { response ->
            // Then - Verify version format is semantic versioning compatible
            val recordedRequest = mockServer.takeRequest()
            val userAgent = recordedRequest.getHeader("User-Agent")!!
            val version = userAgent.substringAfter("$USER_AGENT_PREFIX/")

            assertTrue("Version should not be empty", version.isNotEmpty())
            assertTrue("Version should not contain whitespace", version.trim() == version)

            // Version should match semantic versioning pattern (X.Y.Z) or be a valid identifier
            val semverPattern = Regex("^\\d+\\.\\d+\\.\\d+.*$")
            assertTrue(
                "Version should follow semantic versioning or be a valid identifier: $version",
                semverPattern.matches(version) || version.matches(Regex("^[a-zA-Z0-9._-]+$"))
            )
        }
    }

    @Test
    fun testUserAgentHeaderIsPersistentAcrossRequests() {
        // Given - Create a client with the user agent interceptor
        val interceptor = FlagsmithRetrofitService.userAgentInterceptor(null)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        // When - Make multiple requests
        val request1 = Request.Builder().url(mockServer.url("/first")).build()
        val request2 = Request.Builder().url(mockServer.url("/second")).build()

        client.newCall(request1).execute().close()
        client.newCall(request2).execute().close()

        // Then - Both requests should have the same User-Agent
        val recordedRequest1 = mockServer.takeRequest()
        val recordedRequest2 = mockServer.takeRequest()

        val userAgent1 = recordedRequest1.getHeader("User-Agent")
        val userAgent2 = recordedRequest2.getHeader("User-Agent")

        assertEquals(
            "User-Agent should be consistent across requests",
            userAgent1,
            userAgent2
        )

        assertEquals(
            "User-Agent should be the expected value",
            "$USER_AGENT_PREFIX/$EXPECTED_SDK_VERSION",
            userAgent1
        )
    }
}
