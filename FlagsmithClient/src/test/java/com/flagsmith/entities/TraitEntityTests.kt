package com.flagsmith.entities

import com.flagsmith.Flagsmith
import com.flagsmith.FlagsmithCacheConfig
import com.flagsmith.getTraitSync
import com.flagsmith.mockResponses.MockEndpoint
import com.flagsmith.mockResponses.mockResponseFor
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer

class TraitEntityTests {

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
    fun testTraitValueStringType() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES_TRAIT_STRING)
        runBlocking {
            val result = flagsmith.getTraitSync("client-key", "person")
            Assert.assertTrue(result.isSuccess)
            Assert.assertEquals("12345", result.getOrThrow()?.stringValue)
        }
    }

    @Test
    fun testTraitValueIntType() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES_TRAIT_INTEGER)
        runBlocking {
            val result = flagsmith.getTraitSync("client-key", "person")
            Assert.assertTrue(result.isSuccess)
            Assert.assertEquals(5, result.getOrThrow()?.intValue)
            Assert.assertTrue("Integers in the JSON actually get decoded as Double",
                (result.getOrThrow()?.traitValue) is Double)
        }
    }

    @Test
    fun testTraitValueDoubleType() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES_TRAIT_DOUBLE)
        runBlocking {
            val result = flagsmith.getTraitSync("client-key", "person")
            Assert.assertTrue(result.isSuccess)
            Assert.assertEquals(0.5, result.getOrThrow()?.doubleValue)
        }
    }

    @Test
    fun testTraitValueBooleanType() {
        mockServer.mockResponseFor(MockEndpoint.GET_IDENTITIES_TRAIT_BOOLEAN)
        runBlocking {
            val result = flagsmith.getTraitSync("client-key", "person")
            Assert.assertTrue(result.isSuccess)
            Assert.assertEquals(true, result.getOrThrow()?.booleanValue)
        }
    }

    @Test
    fun testTraitConstructorStringType() {
        val trait = Trait( "string-key", "string-value")
        Assert.assertEquals("string-value", trait.traitValue)
        Assert.assertEquals("string-value", trait.stringValue)
        Assert.assertNull(trait.intValue)

        val traitWithIdentity = TraitWithIdentity("string-key", "string-value", Identity("person"))
        Assert.assertEquals("string-value", traitWithIdentity.traitValue)
        Assert.assertEquals("string-value", traitWithIdentity.stringValue)
        Assert.assertNull(traitWithIdentity.intValue)
    }

    @Test
    fun testTraitConstructorIntType() {
        val trait = Trait("string-key", 1)
        Assert.assertEquals(1, trait.traitValue)
        Assert.assertEquals(1, trait.intValue)
        Assert.assertNull("Can't convert an int to a double", trait.doubleValue)
        Assert.assertNull(trait.stringValue)
        Assert.assertEquals("We should maintain the original functionality for the String .value",
            "1", trait.value)

        val traitWithIdentity = TraitWithIdentity("string-key", 1, Identity("person"))
        Assert.assertEquals(1, traitWithIdentity.traitValue)
        Assert.assertEquals(1, traitWithIdentity.intValue)
        Assert.assertNull("Can't convert an int to a double", traitWithIdentity.doubleValue)
        Assert.assertNull(traitWithIdentity.stringValue)
        Assert.assertEquals("We should maintain the original functionality for the String .value",
            "1", traitWithIdentity.value)
    }

    @Test
    fun testTraitConstructorDoubleType() {
        val trait = Trait("string-key", 1.0)
        Assert.assertEquals(1.0, trait.traitValue)
        Assert.assertEquals(1.0, trait.doubleValue)
        Assert.assertEquals("JS ints are actually doubles so we should handle this",
            1, trait.intValue)
        Assert.assertNull(trait.stringValue)
        Assert.assertEquals("We should maintain the original functionality for the String .value",
            "1.0", trait.value)

        val traitWithIdentity = TraitWithIdentity("string-key", 1.0, Identity("person"))
        Assert.assertEquals(1.0, traitWithIdentity.traitValue)
        Assert.assertEquals(1.0, traitWithIdentity.doubleValue)
        Assert.assertEquals("JS ints are actually doubles so we should handle this",
            1, traitWithIdentity.intValue)
        Assert.assertNull(traitWithIdentity.stringValue)
        Assert.assertEquals("We should maintain the original functionality for the String .value",
            "1.0", traitWithIdentity.value)
    }

    @Test
    fun testTraitConstructorBooleanType() {
        val trait = Trait("string-key", true)
        Assert.assertEquals(true, trait.traitValue)
        Assert.assertEquals(true, trait.booleanValue)
        Assert.assertNull(trait.intValue)
        Assert.assertNull(trait.doubleValue)
        Assert.assertNull(trait.stringValue)
        Assert.assertEquals("We should maintain the original functionality for the String .value",
            "true", trait.value)
    }
}