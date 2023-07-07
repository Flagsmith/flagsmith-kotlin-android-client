package com.flagsmith

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import com.flagsmith.entities.Flag
import com.flagsmith.mockResponses.*
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockserver.integration.ClientAndServer
import java.io.File
import java.time.Duration


class FeatureFlagCachingTests {
    private lateinit var mockServer: ClientAndServer
    private lateinit var mockFailureServer: ClientAndServer
    private lateinit var flagsmithWithCache: Flagsmith
    private lateinit var flagsmithNoCache: Flagsmith

    @Mock
    private lateinit var mockApplicationContext: Context

    @Mock
    private lateinit var mockContextResources: Resources

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Before
    fun setup() {
        mockServer = ClientAndServer.startClientAndServer()
        System.setProperty("mockserver.logLevel", "INFO")
        Awaitility.setDefaultTimeout(Duration.ofSeconds(30));
        setupMocks()

        flagsmithWithCache = Flagsmith(
            environmentKey = "",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            enableCache = true,
            context = mockApplicationContext
        )

        flagsmithNoCache = Flagsmith(
            environmentKey = "",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            enableCache = false
        )
    }

    private fun setupMocks() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // During unit testing sometimes test fails because of your methods
        // are using the app Context to retrieve resources, but during unit test the Context is null
        // so we can mock it.


        // During unit testing sometimes test fails because of your methods
        // are using the app Context to retrieve resources, but during unit test the Context is null
        // so we can mock it.
        `when`(mockApplicationContext.getResources()).thenReturn(mockContextResources)
        `when`(mockApplicationContext.getSharedPreferences(anyString(), anyInt())).thenReturn(
            mockSharedPreferences
        )
        `when`(mockApplicationContext.cacheDir).thenReturn(File("cache"))

        `when`(mockContextResources.getString(anyInt())).thenReturn("mocked string")
        `when`(mockContextResources.getStringArray(anyInt())).thenReturn(
            arrayOf(
                "mocked string 1",
                "mocked string 2"
            )
        )
        `when`(mockContextResources.getColor(anyInt())).thenReturn(Color.BLACK)
        `when`(mockContextResources.getBoolean(anyInt())).thenReturn(false)
        `when`(mockContextResources.getDimension(anyInt())).thenReturn(100f)
        `when`(mockContextResources.getIntArray(anyInt())).thenReturn(intArrayOf(1, 2, 3))
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @Test
    fun testThrowsExceptionWhenEnableCachingWithoutAContext() {
        val exception = Assert.assertThrows(IllegalArgumentException::class.java) {
            val flagsmith = Flagsmith(
                environmentKey = "",
                baseUrl = "http://localhost:${mockServer.localPort}",
                enableAnalytics = false
            )
        }
        Assert.assertEquals(
            "Flagsmith requires a context to use the cache feature",
            exception.message
        )
    }

    @Test
    fun testGetFeatureFlagsWithIdentityUsesCachedResponseOnSecondRequestFailure() {
        Fuel.trace = true
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)

        try {
            // First time around we should be successful and cache the response
            var foundFromServer: Flag? = null
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromServer = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromServer)
                Assert.assertEquals(756.0, foundFromServer?.featureStateValue)
            }

            await untilNotNull { foundFromServer }

            // Now we mock the failure and expect the cached response to be returned
            var foundFromCache: Flag? = null
//            mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromCache = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromCache)
                Assert.assertEquals(756.0, foundFromCache?.featureStateValue)
            }

            await untilNotNull { foundFromCache }

        } catch (e: Exception) {
            Log.e("testGetFeatureFlagsTimeoutAwaitability", "error: $e")
            Assert.fail()
        }
    }

    @Test
    fun testGetFeatureFlagsWithIdentityUsesCachedResponseOnSecondRequestTimeout() {
        Fuel.trace = true
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        mockServer.mockDelayFor(MockEndpoint.GET_IDENTITIES)

        try {
            // First time around we should be successful and cache the response
            var foundFromServer: Flag? = null
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromServer = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromServer)
                Assert.assertEquals(756.0, foundFromServer?.featureStateValue)
            }

            await untilNotNull { foundFromServer }

            // Now we mock the failure and expect the cached response to be returned
            var foundFromCache: Flag? = null
//            mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromCache = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromCache)
                Assert.assertEquals(756.0, foundFromCache?.featureStateValue)
            }

            await untilNotNull { foundFromCache }

        } catch (e: Exception) {
            Log.e("testGetFeatureFlagsTimeoutAwaitability", "error: $e")
            Assert.fail()
        }
    }

    @Test
    fun testGetFeatureFlagsNoIdentityUsesCachedResponseOnSecondRequestFailure() {
        Fuel.trace = true
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        mockServer.mockFailureFor(MockEndpoint.GET_FLAGS)

        try {
            // First time around we should be successful and cache the response
            var foundFromServer: Flag? = null
            flagsmithWithCache.getFeatureFlags() { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromServer = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromServer)
                Assert.assertEquals(7.0, foundFromServer?.featureStateValue)
            }

            await untilNotNull { foundFromServer }

            // Now we mock the failure and expect the cached response to be returned
            var foundFromCache: Flag? = null
            flagsmithWithCache.getFeatureFlags() { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromCache = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromCache)
                Assert.assertEquals(7.0, foundFromCache?.featureStateValue)
            }

            await untilNotNull { foundFromCache }

        } catch (e: Exception) {
            Log.e("testGetFeatureFlagsNoIdentityUsesCachedResponseOnSecondRequestFailure", "error: $e")
            Assert.fail()
        }
    }
}