package com.flagsmith.entities


import com.google.gson.annotations.SerializedName

data class TraitModel(
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val value: String
)
