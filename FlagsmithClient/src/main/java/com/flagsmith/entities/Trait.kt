package com.flagsmith.entities

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Reader

class TraitWithIdentityDeserializer: ResponseDeserializable<TraitWithIdentity> {
    override fun deserialize(reader: Reader): TraitWithIdentity? {
        return Gson().fromJson(reader, TraitWithIdentity::class.java)
    }
}

data class Trait(
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val value: String
)

data class TraitWithIdentity(
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val value: String,
    val identity: Identity
)
