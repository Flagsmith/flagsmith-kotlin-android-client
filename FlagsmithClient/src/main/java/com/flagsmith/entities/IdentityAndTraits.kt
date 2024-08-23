package com.flagsmith.entities

import com.google.gson.annotations.SerializedName

data class IdentityAndTraits(
    @SerializedName(value = "identifier") val identifier: String,
    @SerializedName(value = "traits") val traits: List<Trait>,
    @SerializedName(value = "transient") val transient: Boolean? = null
)