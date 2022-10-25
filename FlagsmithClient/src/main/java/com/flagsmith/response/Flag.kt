package com.flagsmith.response

import com.google.gson.annotations.SerializedName


data class Flag(
    val feature: Feature,
    @SerializedName(value = "feature_state_value") val featureStateValue: Any?,
    val enabled: Boolean
)

data class Feature(
    val id: Long,
    val name: String,
    @SerializedName(value = "created_date") val createdDate: String,
    val description: String,
    @SerializedName(value = "initial_value") val initialValue: String,
    @SerializedName(value = "default_enabled") val defaultEnabled: Boolean,
    val type: String
)