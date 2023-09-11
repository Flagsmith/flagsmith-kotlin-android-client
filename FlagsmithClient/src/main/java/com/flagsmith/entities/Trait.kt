package com.flagsmith.entities


import com.google.gson.annotations.SerializedName

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
