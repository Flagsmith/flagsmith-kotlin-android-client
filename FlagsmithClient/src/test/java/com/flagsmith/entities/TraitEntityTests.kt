package com.flagsmith.entities

import org.junit.Assert
import org.junit.Test

class TraitEntityTests {
    @Test
    fun testTraitEntity() {
        val trait = Trait("trait_key", "trait_value")
        Assert.assertEquals("trait_key", trait.key)
        Assert.assertEquals("trait_value", trait.value)
    }

    @Test
    fun testTraitValueStringType() {
        val trait = Trait("trait_key", "trait_value")
        Assert.assertEquals("trait_value", trait.stringValue)
        Assert.assertNull(trait.intValue)
        Assert.assertNull(trait.doubleValue)
        Assert.assertNull(trait.booleanValue)
    }

    @Test
    fun testTraitValueIntType() {
        val trait = Trait("trait_key", 1)
        Assert.assertEquals(1, trait.intValue)
        Assert.assertNull(trait.stringValue)
        Assert.assertNull(trait.doubleValue)
        Assert.assertNull(trait.booleanValue)
    }

    @Test
    fun testTraitValueDoubleType() {
        val trait = Trait("trait_key", 0.5)
        Assert.assertEquals(0.5, trait.doubleValue)
        Assert.assertNull(trait.stringValue)
        Assert.assertNull(trait.intValue)
        Assert.assertNull(trait.booleanValue)
    }

    @Test
    fun testTraitValueBooleanType() {
        val trait = Trait("trait_key", true)
        Assert.assertEquals(true, trait.booleanValue)
        Assert.assertNull(trait.intValue)
        Assert.assertNull(trait.intValue)
        Assert.assertNull(trait.doubleValue)
    }
}