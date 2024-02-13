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
}