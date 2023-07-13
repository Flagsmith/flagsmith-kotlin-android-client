package com.flagsmith

import com.flagsmith.mockResponses.MockEndpoint
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
}