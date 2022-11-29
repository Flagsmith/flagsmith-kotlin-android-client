package com.flagsmith

import com.flagsmith.entities.Trait
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TraitsTests {

    private val flagsmith = Flagsmith(environmentKey = System.getenv("ENVIRONMENT_KEY") ?: "", enableAnalytics = false)

    @Test
    fun testGetTraitsDefinedForPerson() {
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertEquals(result.getOrThrow().find { trait -> trait.key == "favourite-colour" }?.value, "electric pink")
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
            assertEquals(result.getOrThrow()?.value, "electric pink")
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
            assertEquals(result.getOrThrow().key, "set-from-client")
            assertEquals(result.getOrThrow().value, "12345")
            assertEquals(result.getOrThrow().identity.identifier, "person")
        }
    }

    @Test
    fun testGetIdentity() {
        runBlocking {
            val result = flagsmith.getIdentitySync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().traits.isNotEmpty())
            assertTrue(result.getOrThrow().flags.isNotEmpty())
            assertEquals(result.getOrThrow().traits.find { trait -> trait.key == "favourite-colour" }?.value, "electric pink")
        }
    }
}