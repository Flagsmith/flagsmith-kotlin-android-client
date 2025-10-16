package com.flagsmith

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.junit.runner.RunWith

@RunWith(MockitoJUnitRunner::class)
class UserAgentTests {

    private lateinit var mockServer: ClientAndServer
    private lateinit var flagsmith: Flagsmith

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockPackageInfo: PackageInfo

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
        // Given - Use a realistic app version (not SDK version)
        // The User-Agent shows the APP's version, not the SDK's version
        // This helps Flagsmith support team identify which app version is making requests
        val expectedAppVersion = "2.4.1"
        mockPackageInfo.versionName = expectedAppVersion
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockContext.packageName).thenReturn("com.test.app")
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0)).thenReturn(mockPackageInfo)

        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = mockContext,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)

        // When
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)
        }

        // Then - Verify User-Agent contains the APP's version, not SDK version
        mockServer.verify(
            request()
                .withPath("/flags/")
                .withMethod("GET")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/$expectedAppVersion")
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
        // Given
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockContext.packageName).thenReturn("com.test.app")
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0))
            .thenThrow(PackageManager.NameNotFoundException("Package not found"))

        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = mockContext,
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
        // Given
        mockPackageInfo.versionName = null
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockContext.packageName).thenReturn("com.test.app")
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0)).thenReturn(mockPackageInfo)

        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = mockContext,
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
        // Given
        val expectedVersion = "2.1.0"
        mockPackageInfo.versionName = expectedVersion
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockContext.packageName).thenReturn("com.test.app")
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0)).thenReturn(mockPackageInfo)

        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = mockContext,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)

        // When
        runBlocking {
            val result = flagsmith.getIdentitySync("test-user")
            assertTrue(result.isSuccess)
        }

        // Then
        mockServer.verify(
            request()
                .withPath("/identities/")
                .withMethod("GET")
                .withQueryStringParameter("identifier", "test-user")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/$expectedVersion")
        )
    }

    @Test
    fun testUserAgentHeaderSentWithTraitRequest() {
        // Given
        val expectedAppVersion = "3.0.1"
        mockPackageInfo.versionName = expectedAppVersion
        Mockito.`when`(mockContext.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockContext.packageName).thenReturn("com.test.app")
        Mockito.`when`(mockPackageManager.getPackageInfo(mockContext.packageName, 0)).thenReturn(mockPackageInfo)

        flagsmith = Flagsmith(
            environmentKey = "test-key",
            baseUrl = "http://localhost:${mockServer.localPort}",
            context = mockContext,
            enableAnalytics = false,
            cacheConfig = FlagsmithCacheConfig(enableCache = false)
        )

        mockServer.mockResponseFor(MockEndpoint.SET_TRAIT)

        // When
        runBlocking {
            val result = flagsmith.setTraitSync(Trait(key = "test-key", traitValue = "test-value"), "test-user")
            assertTrue(result.isSuccess)
        }

        // Then - Verify the traits request has correct User-Agent
        mockServer.verify(
            request()
                .withPath("/identities/")
                .withMethod("POST")
                .withHeader("User-Agent", "flagsmith-kotlin-android-sdk/$expectedAppVersion")
        )
    }
}