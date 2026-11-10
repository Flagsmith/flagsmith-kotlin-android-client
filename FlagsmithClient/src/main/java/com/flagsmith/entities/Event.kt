package com.flagsmith.entities

import com.google.gson.annotations.SerializedName

data class Event(
    val event: String,
    @SerializedName(value = "feature_name") val featureName: String?,
    val identifier: String?,
    val value: String?,
    val traits: Map<String, Any?>?,
    val metadata: Map<String, Any?>,
    /** Milliseconds since the epoch. */
    val timestamp: Long
)

data class EventBatch(val events: List<Event>)
