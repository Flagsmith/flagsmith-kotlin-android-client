package com.flagsmith.entities

import com.flagsmith.internal.Deserializer
import com.flagsmith.internal.fromJson
import com.github.kittinunf.fuse.core.Fuse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

class IdentityFlagsAndTraitsDeserializer: Deserializer<IdentityFlagsAndTraits> {
    override fun deserialize(reader: Reader): IdentityFlagsAndTraits? =
        reader.fromJson(IdentityFlagsAndTraits::class.java)
}

class IdentityFlagsAndTraitsDataConvertible: Fuse.DataConvertible<IdentityFlagsAndTraits> {
    override fun convertFromData(bytes: ByteArray): IdentityFlagsAndTraits =
        Gson().fromJson(String(bytes), IdentityFlagsAndTraits::class.java)

    override fun convertToData(value: IdentityFlagsAndTraits): ByteArray =
        Gson().toJson(value).toByteArray()
}

data class IdentityFlagsAndTraits(
    val flags: ArrayList<Flag>,
    val traits: ArrayList<Trait>
)