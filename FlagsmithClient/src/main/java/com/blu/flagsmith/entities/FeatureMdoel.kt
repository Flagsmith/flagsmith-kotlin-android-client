package com.blu.flagsmith.entities

import com.google.gson.annotations.SerializedName

data class FeatureModel(
    val id: Long,
    val name: String,
    @SerializedName(value = "created_date") val createdDate: String,
    val description: String,
    @SerializedName(value = "initial_value") val initialValue: String,
    @SerializedName(value = "default_enabled") val defaultEnabled: Boolean,
    val type: String
)
