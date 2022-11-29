package com.flagsmith

import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FeatureFlagTests {

    private val flagsmith = Flagsmith(environmentKey = System.getenv("ENVIRONMENT_KEY") ?: "", enableAnalytics = false)

    @Test
    fun testHasFeatureFlagWithFlag() {
        runBlocking {
            val result = flagsmith.hasFeatureFlagSync("no-value")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow())
        }
    }

    @Test
    fun testHasFeatureFlagWithoutFlag() {
        runBlocking {
            val result = flagsmith.hasFeatureFlagSync("doesnt-exist")
            assertTrue(result.isSuccess)
            assertFalse(result.getOrThrow())
        }
    }

    @Test
    fun testGetFeatureFlags() {
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync()
            assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            assertNotNull(found)
            assertEquals(found?.featureStateValue, 7.0)
        }
    }

    @Test
    fun testGetFeatureFlagsWithIdentity() {
        runBlocking {
            val result = flagsmith.getFeatureFlagsSync(identity = "person")
            assertTrue(result.isSuccess)

            val found = result.getOrThrow().find { flag -> flag.feature.name == "with-value" }
            assertNotNull(found)
            assertEquals(found?.featureStateValue, 756.0)
        }
    }

    @Test
    fun testGetValueForFeatureExisting() {
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("with-value", identity = null)
            assertTrue(result.isSuccess)
            assertEquals(result.getOrThrow(), 7.0)
        }
    }

    @Test
    fun testGetValueForFeatureExistingOverriddenWithIdentity() {
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("with-value", identity = "person")
            assertTrue(result.isSuccess)
            assertEquals(result.getOrThrow(), 756.0)
        }
    }

    @Test
    fun testGetValueForFeatureNotExisting() {
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("not-existing", identity = null)
            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }
    }

    @Test
    fun testHasFeatureForNoIdentity() {
        runBlocking {
            val result = flagsmith.hasFeatureFlagSync("with-value-just-person-enabled", identity = null)
            assertTrue(result.isSuccess)
            assertFalse(result.getOrThrow())
        }
    }

    @Test
    fun testHasFeatureWithIdentity() {
        runBlocking {
            val result = flagsmith.hasFeatureFlagSync("with-value-just-person-enabled", identity = "person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow())
        }
    }
}