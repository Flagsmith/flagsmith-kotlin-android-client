package com.flagsmith.entities

import com.google.gson.annotations.SerializedName

data class Flag(
    val feature: Feature,
    @SerializedName(value = "feature_state_value") val featureStateValue: Any?,
    val enabled: Boolean
)

data class Feature(
    val name: String,
    val id: Long = 0L,
    @SerializedName(value = "created_date") val createdDate: String = "",
    val description: String = "",
    @SerializedName(value = "initial_value") val initialValue: String = "",
    @SerializedName(value = "default_enabled") val defaultEnabled: Boolean = false,
    val type: String = ""
)
