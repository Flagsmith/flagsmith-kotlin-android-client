package com.flagsmith

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
            assertEquals(7.0, found?.featureStateValue)
        }
    }

    @Test
    fun testGetFeatureFlagsWithIdentity() {
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
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("with-value", identity = null)
            assertTrue(result.isSuccess)
            assertEquals(7.0, result.getOrThrow())
        }
    }

    @Test
    fun testGetValueForFeatureExistingOverriddenWithIdentity() {
        runBlocking {
            val result = flagsmith.getValueForFeatureSync("with-value", identity = "person")
            assertTrue(result.isSuccess)
            assertEquals(756.0, result.getOrThrow())
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