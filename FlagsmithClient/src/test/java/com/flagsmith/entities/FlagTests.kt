package com.flagsmith.entities

import org.junit.Assert.assertNotNull
import org.junit.Test

class FlagTests {
    @Test
    fun `Flag keeps the constructor Java callers used before reason was added`() {
        assertNotNull(
            Flag::class.java.getConstructor(
                Feature::class.java,
                Any::class.java,
                Boolean::class.javaPrimitiveType,
                String::class.java,
                FlagMetadata::class.java
            )
        )
    }
}
