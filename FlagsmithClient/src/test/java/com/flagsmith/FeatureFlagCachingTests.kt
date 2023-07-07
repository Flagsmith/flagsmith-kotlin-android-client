package com.flagsmith

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import com.flagsmith.entities.Feature
import com.flagsmith.entities.Flag
import com.flagsmith.mockResponses.*
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
        val defaultFlags = listOf(
            Flag(
                feature = Feature(
                    id = 345345L,
                    name = "Flag 1",
                    createdDate = "2023‐07‐07T09:07:16Z",
                    description = "Flag 1 description",
                    type = "CONFIG",
                    defaultEnabled = true,
                    initialValue = "true"
                ), enabled = true, featureStateValue = "value1"
            ),
            Flag(
                feature = Feature(
                    id = 34345L,
                    name = "Flag 2",
                    createdDate = "2023‐07‐07T09:07:16Z",
                    description = "Flag 2 description",
                    type = "CONFIG",
                    defaultEnabled = true,
                    initialValue = "true"
                ), enabled = true, featureStateValue = "value1"
            ),
        )

        flagsmithWithCache = Flagsmith(
            environmentKey = "",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            enableCache = true,
            context = mockApplicationContext,
            defaultFlags = defaultFlags
        )

        flagsmithNoCache = Flagsmith(
            environmentKey = "",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            enableCache = false,
            defaultFlags = defaultFlags
        )
    }

    private fun setupMocks() {
        MockitoAnnotations.initMocks(this)

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
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)

        try {
            // First time around we should be successful and cache the response
            var foundFromServer: Flag? = null
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromServer =
                    result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromServer)
                Assert.assertEquals(756.0, foundFromServer?.featureStateValue)
            }

            await untilNotNull { foundFromServer }

            // Now we mock the failure and expect the cached response to be returned
            var foundFromCache: Flag? = null
//            mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromCache =
                    result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
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
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        mockServer.mockDelayFor(MockEndpoint.GET_IDENTITIES)

        try {
            // First time around we should be successful and cache the response
            var foundFromServer: Flag? = null
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromServer =
                    result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromServer)
                Assert.assertEquals(756.0, foundFromServer?.featureStateValue)
            }

            await untilNotNull { foundFromServer }

            // Now we mock the failure and expect the cached response to be returned
            var foundFromCache: Flag? = null
//            mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromCache =
                    result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
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
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)
        mockServer.mockFailureFor(MockEndpoint.GET_FLAGS)

        try {
            // First time around we should be successful and cache the response
            var foundFromServer: Flag? = null
            flagsmithWithCache.getFeatureFlags() { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromServer =
                    result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromServer)
                Assert.assertEquals(7.0, foundFromServer?.featureStateValue)
            }

            await untilNotNull { foundFromServer }

            // Now we mock the failure and expect the cached response to be returned
            var foundFromCache: Flag? = null
            flagsmithWithCache.getFeatureFlags() { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromCache =
                    result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromCache)
                Assert.assertEquals(7.0, foundFromCache?.featureStateValue)
            }

            await untilNotNull { foundFromCache }

        } catch (e: Exception) {
            Log.e(
                "testGetFeatureFlagsNoIdentityUsesCachedResponseOnSecondRequestFailure",
                "error: $e"
            )
            Assert.fail()
        }
    }

    @Test
    fun testGetFlagsWithFailingRequestShouldGetDefaults() {
        mockServer.mockFailureFor(MockEndpoint.GET_FLAGS)
        mockServer.mockResponseFor(MockEndpoint.GET_FLAGS)

        try {
            // First time around we should fail and fall back to the defaults
            var foundFromCache: Flag? = null
            flagsmithWithCache.getFeatureFlags() { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromCache =
                    result.getOrThrow().find { flag -> flag.feature.name == "Flag 1" }
                Assert.assertNotNull(foundFromCache)
            }

            await untilNotNull { foundFromCache }

            // Now we mock the server and expect the server response to be returned
            var foundFromServer: Flag? = null
            flagsmithWithCache.getFeatureFlags() { result ->
                Assert.assertTrue(result.isSuccess)

                foundFromServer =
                    result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(foundFromServer)
                Assert.assertEquals(7.0, foundFromServer?.featureStateValue)
            }

            await untilNotNull { foundFromServer }

        } catch (e: Exception) {
            Log.e(
                "testGetFeatureFlagsNoIdentityUsesCachedResponseOnSecondRequestFailure",
                "error: $e"
            )
            Assert.fail()
        }
    }

    @Test
    fun testGetFlagsWithTimeoutRequestShouldGetDefaults() {

    }
}