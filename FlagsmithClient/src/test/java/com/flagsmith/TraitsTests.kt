package com.flagsmith

import com.flagsmith.entities.Trait
import com.flagsmith.mockResponses.MockEndpoint
import com.flagsmith.mockResponses.mockResponseFor
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer

class TraitsTests {

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
    fun testGetTraitsDefinedForPerson() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertEquals(
                "electric pink",
                result.getOrThrow().find { trait -> trait.key == "favourite-colour" }?.stringValue
            )
        }
    }

    @Test
    fun testGetTraitsNotDefinedForPerson() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmith.getTraitsSync("person")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isNotEmpty())
            assertNull(result.getOrThrow().find { trait -> trait.key == "fake-trait" }?.stringValue)
        }
    }

    @Test
    fun testGetTraitById() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmith.getTraitSync("favourite-colour", "person")
            assertTrue(result.isSuccess)
            assertEquals("electric pink", result.getOrThrow()?.stringValue)
        }
    }

    @Test
    fun testGetUndefinedTraitById() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES)
        runBlocking {
            val result = flagsmith.getTraitSync("favourite-cricketer", "person")
            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }
    }

    @Test
    fun testSetTrait() {
        mockServer.mockResponseFor(MockEndpoint.SET_TRAIT)
        runBlocking {
            val result =
                flagsmith.setTraitSync(Trait(key = "set-from-client", value = "12345"), "person")
            assertTrue(result.isSuccess)
            assertEquals("set-from-client", result.getOrThrow().key)
            assertEquals("12345", result.getOrThrow().stringValue)
            assertEquals("person", result.getOrThrow().identity.identifier)
        }
    }

    @Test
    fun testSetTraits() {
        mockServer.mockResponseFor(MockEndpoint.SET_TRAITS)
        runBlocking {
            val result =
                flagsmith.setTraitsSync(listOf(Trait(key = "set-from-client", value = "12345")), "person")
            assertTrue(result.isSuccess)
            assertEquals("set-from-client", result.getOrThrow().first().key)
            assertEquals("12345", result.getOrThrow().first().stringValue)
            assertEquals("person", result.getOrThrow().first().identity.identifier)
        }
    }

    @Test
    fun testSetTraitInteger() {
        mockServer.mockResponseFor(MockEndpoint.SET_TRAIT_INTEGER)
        runBlocking {
            val result =
                flagsmith.setTraitSync(Trait(key = "set-from-client", value = 5), "person")
            assertTrue(result.isSuccess)
            assertEquals("set-from-client", result.getOrThrow().key)
            assertEquals(5, result.getOrThrow().intValue)
            assertEquals("person", result.getOrThrow().identity.identifier)
        }
    }

    @Test
    fun testSetTraitDouble() {
        mockServer.mockResponseFor(MockEndpoint.SET_TRAIT_DOUBLE)
        runBlocking {
            val result =
                flagsmith.setTraitSync(Trait(key = "set-from-client", value = 0.5), "person")
            assertTrue(result.isSuccess)
            assertEquals("set-from-client", result.getOrThrow().key)
            assertEquals(0.5, result.getOrThrow().doubleValue)
            assertEquals("person", result.getOrThrow().identity.identifier)
        }
    }

    @Test
    fun testSetTraitBoolean() {
        mockServer.mockResponseFor(MockEndpoint.SET_TRAIT_BOOLEAN)
        runBlocking {
            val result =
                flagsmith.setTraitSync(Trait(key = "set-from-client", value = true), "person")
            assertTrue(result.isSuccess)
            assertEquals("set-from-client", result.getOrThrow().key)
            assertEquals(true, result.getOrThrow().booleanValue)
            assertEquals("person", result.getOrThrow().identity.identifier)
        }
    }
}
