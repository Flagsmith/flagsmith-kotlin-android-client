package com.blu.flagsmith.entities

import com.flagsmith.entities.Identity
import com.flagsmith.entities.IdentityModel
import com.google.gson.annotations.SerializedName

data class TraitWithIdentityModel(
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val value: String,
    val identity: IdentityModel
)
