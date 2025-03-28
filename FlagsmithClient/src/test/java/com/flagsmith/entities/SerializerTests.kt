package com.flagsmith.entities

import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class SerializerTests {
    private val gson = GsonBuilder()
        .registerTypeAdapter(IdentityAndTraits::class.java, IdentityAndTraitsSerializer())
        .registerTypeAdapter(Trait::class.java, TraitSerializer())
        .create()

    @Test
    fun `IdentityAndTraitsSerializer omits transient field when false`() {
        val identity = IdentityAndTraits(
            identifier = "test-user",
            traits = listOf(Trait("key", "value")),
            transient = false
        )

        val json = gson.toJson(identity)
        assertEquals(
            """{"identifier":"test-user","traits":[{"trait_key":"key","trait_value":"value"}]}""",
            json
        )
    }

    @Test
    fun `IdentityAndTraitsSerializer includes transient field when true`() {
        val identity = IdentityAndTraits(
            identifier = "test-user",
            traits = listOf(Trait("key", "value")),
            transient = true
        )

        val json = gson.toJson(identity)
        assertEquals(
            """{"identifier":"test-user","traits":[{"trait_key":"key","trait_value":"value"}],"transient":true}""",
            json
        )
    }

    @Test
    fun `TraitSerializer omits transient field when false`() {
        val trait = Trait("key", "value", false)
        val json = gson.toJson(trait)
        assertEquals(
            """{"trait_key":"key","trait_value":"value"}""",
            json
        )
    }

    @Test
    fun `TraitSerializer includes transient field when true`() {
        val trait = Trait("key", "value", true)
        val json = gson.toJson(trait)
        assertEquals(
            """{"trait_key":"key","trait_value":"value","transient":true}""",
            json
        )
    }

    @Test
    fun `TraitSerializer handles optional identifier`() {
        val traitWithId = Trait(
            identifier = "test-id",
            key = "key",
            traitValue = "value",
            transient = false
        )
        val traitWithoutId = Trait("key", "value", false)

        assertEquals(
            """{"identifier":"test-id","trait_key":"key","trait_value":"value"}""",
            gson.toJson(traitWithId)
        )
        assertEquals(
            """{"trait_key":"key","trait_value":"value"}""",
            gson.toJson(traitWithoutId)
        )
    }
} 
