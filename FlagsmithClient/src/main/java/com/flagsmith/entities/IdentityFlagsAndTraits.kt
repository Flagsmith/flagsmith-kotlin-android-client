package com.flagsmith.entities

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import java.io.Reader

class IdentityFlagsAndTraitsDeserializer: ResponseDeserializable<IdentityFlagsAndTraits> {
    override fun deserialize(reader: Reader): IdentityFlagsAndTraits? {
        return Gson().fromJson(reader, IdentityFlagsAndTraits::class.java)
    }
}

data class IdentityFlagsAndTraits(
    val flags: ArrayList<Flag>,
    val traits: ArrayList<Trait>
)