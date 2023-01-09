package com.flagsmith.entities

import com.flagsmith.internal.Deserializer
import com.flagsmith.internal.fromJson
import java.io.Reader

class IdentityFlagsAndTraitsDeserializer: Deserializer<IdentityFlagsAndTraits> {
    override fun deserialize(reader: Reader): IdentityFlagsAndTraits? =
        reader.fromJson(IdentityFlagsAndTraits::class.java)
}

data class IdentityFlagsAndTraits(
    val flags: ArrayList<Flag>,
    val traits: ArrayList<Trait>
)