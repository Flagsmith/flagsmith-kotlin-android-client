package com.flagsmith.android.flagsmith.response

import com.google.gson.annotations.SerializedName

data class ResponseFlagElement (
    val id: Long,
    val feature: Feature,
    val featureStateValue: String,
    val environment: Long,
    val identity: Any? = null,
    val featureSegment: Any? = null,
    val enabled: Boolean
)

data class Feature (
    val id: Long,
    val name: String,
    @SerializedName(value="created_date") val createdDate: String,
    val description: String,
    @SerializedName(value="initial_value")  val initialValue: String,
    @SerializedName(value="default_enabled") val defaultEnabled: Boolean,
    val type: String
)
