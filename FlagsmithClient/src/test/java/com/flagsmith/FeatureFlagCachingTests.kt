package com.flagsmith

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
import org.mockserver.integration.ClientAndServer
import java.time.Duration

class FeatureFlagCachingTests {
    private lateinit var mockServer: ClientAndServer
    private lateinit var mockFailureServer: ClientAndServer
    private lateinit var flagsmithWithCache: Flagsmith
    private lateinit var flagsmithNoCache: Flagsmith

    @Before
    fun setup() {
        mockServer = ClientAndServer.startClientAndServer()
        System.setProperty("mockserver.logLevel", "INFO")
        Awaitility.setDefaultTimeout(Duration.ofSeconds(30));

        flagsmithWithCache = Flagsmith(
            environmentKey = "",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            enableCache = true
        )

        flagsmithNoCache = Flagsmith(
            environmentKey = "",
            baseUrl = "http://localhost:${mockServer.localPort}",
            enableAnalytics = false,
            enableCache = false
        )
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @Test
    fun testGetFeatureFlagsTimeoutAwaitability() {
        Fuel.trace = true
        mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)
        var found: Flag? = null

        try {
            val running = flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(found)
                Assert.assertEquals(756.0, found?.featureStateValue)
            }

            running.join()
            await untilNotNull { found }
        } catch (e: Exception) {
            Log.e("testGetFeatureFlagsTimeoutAwaitability", "error: $e")
            Assert.fail()
        }
        Log.i("testGetFeatureFlagsTimeoutAwaitability", "found: $found")
    }

    @Test
    fun testGetFeatureFlagsWithIdentity() {
        mockServer.mockDelayFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            // val result = flagsmithWithCache.getFeatureFlagsSync(identity = "person")
            flagsmithWithCache.getFeatureFlags(identity = "person") { result ->
                Assert.assertTrue(result.isSuccess)

                val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(found)
                Assert.assertEquals(756.0, found?.featureStateValue)
            }
//            Assert.assertTrue(result.isSuccess)
//
//            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
//            Assert.assertNotNull(found)
//            Assert.assertEquals(756.0, found?.featureStateValue)

//            val result2 = flagsmithNoCache.getFeatureFlagsSync(identity = "person")
//            Assert.assertTrue(result2.isSuccess)
//
//            val found2 = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
//            Assert.assertNotNull(found2)
//            Assert.assertEquals(756.0, found2?.featureStateValue)
        }
    }

    @Test
    fun testGetFeatureFlagsWithIdentitySameRegardlessOfCaching() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmithWithCache.getFeatureFlagsSync(identity = "person")
            Assert.assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            Assert.assertNotNull(found)
            Assert.assertEquals(756.0, found?.featureStateValue)

            mockServer.stop()
            mockServer = ClientAndServer.startClientAndServer()
            mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)

            val result2 = flagsmithNoCache.getFeatureFlagsSync(identity = "person")
            Assert.assertTrue(result2.isSuccess)

            val found2 = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            Assert.assertNotNull(found2)
            Assert.assertEquals(756.0, found?.featureStateValue)
        }
    }

    @Test
    fun testGetFeatureFlagsWithIdentityUsesCacheOnSecondFailedRequest() {
        Fuel.trace = true
        mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            try {
                val result = flagsmithWithCache.getFeatureFlagsSync(identity = "person")
                Assert.assertTrue(result.isSuccess)

                val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
                Assert.assertNotNull(found)
                Assert.assertEquals(756.0, found?.featureStateValue)
            } catch (e: Exception) {
                Log.e("testGetFeatureFlagsWithIdentityUsesCacheOnSecondFailedRequest", "error: $e")
            }
        }

//        mockServer.stop()
//        mockServer = ClientAndServer.startClientAndServer()
//        mockServer.mockFailureFor(MockEndpoint.GET_IDENTITIES)
//        runBlocking {
//            val result = flagsmithWithCache.getFeatureFlagsSync(identity = "person")
//            Assert.assertTrue(result.isSuccess)
//
//            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
//            Assert.assertNotNull(found)
//            Assert.assertEquals(756.0, found?.featureStateValue)
//        }
    }

    @Test
    fun testGetFeatureFlagsWithIdentityFailsOnSecondFailedRequestWithNoCache() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmithNoCache.getFeatureFlagsSync(identity = "person")
            Assert.assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            Assert.assertNotNull(found)
            Assert.assertEquals(756.0, found?.featureStateValue)
        }

        mockServer.stop()
        mockServer = ClientAndServer.startClientAndServer()
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmithNoCache.getFeatureFlagsSync(identity = "person")
            Assert.assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            Assert.assertNotNull(found)
            Assert.assertEquals(756.0, found?.featureStateValue)
        }
    }
}