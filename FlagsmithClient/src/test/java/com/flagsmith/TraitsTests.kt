package com.flagsmith

import com.flagsmith.entities.Trait
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TraitsTests {

    private val flagsmith = Flagsmith(environmentKey = System.getenv("ENVIRONMENT_KEY") ?: "", enableAnalytics = false)

    @Test
    fun testGetTraitsDefinedForPerson() {
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertEquals("electric pink", result.getOrThrow().find { trait -> trait.key == "favourite-colour" }?.value)
        }
    }

    @Test
    fun testGetTraitsNotDefinedForPerson() {
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertNull(result.getOrThrow().find { trait -> trait.key == "fake-trait" }?.value)
        }
    }

    @Test
    fun testGetTraitById() {
        runBlocking {
            val result = flagsmith.getTraitSync("favourite-colour", "person")
            assertTrue(result.isSuccess)
            assertEquals("electric pink", result.getOrThrow()?.value)
        }
    }

    @Test
    fun testGetUndefinedTraitById() {
        runBlocking {
            val result = flagsmith.getTraitSync("favourite-cricketer", "person")
            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }
    }

    @Test
    fun testSetTrait() {
        runBlocking {
            val result = flagsmith.setTraitSync(Trait(key = "set-from-client", value = "12345"), "person")
            assertTrue(result.isSuccess)
            assertEquals("set-from-client", result.getOrThrow().key)
            assertEquals("12345", result.getOrThrow().value)
            assertEquals("person", result.getOrThrow().identity.identifier)
        }
    }

    @Test
    fun testGetIdentity() {
        runBlocking {
            val result = flagsmith.getIdentitySync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().traits.isNotEmpty())
            assertTrue(result.getOrThrow().flags.isNotEmpty())
            assertEquals("electric pink", result.getOrThrow().traits.find { trait -> trait.key == "favourite-colour" }?.value)
        }
    }
}