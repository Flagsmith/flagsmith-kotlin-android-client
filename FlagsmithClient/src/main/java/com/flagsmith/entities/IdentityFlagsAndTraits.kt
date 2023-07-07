package com.flagsmith.entities

import com.github.kittinunf.fuse.core.Fuse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

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