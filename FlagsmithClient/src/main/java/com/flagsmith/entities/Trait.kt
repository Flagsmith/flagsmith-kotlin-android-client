package com.flagsmith.entities

import com.flagsmith.internal.Deserializer
import com.flagsmith.internal.fromJson
import com.google.gson.annotations.SerializedName
import java.io.Reader

class TraitWithIdentityDeserializer: Deserializer<TraitWithIdentity> {
    override fun deserialize(reader: Reader): TraitWithIdentity? =
        reader.fromJson(TraitWithIdentity::class.java)
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
