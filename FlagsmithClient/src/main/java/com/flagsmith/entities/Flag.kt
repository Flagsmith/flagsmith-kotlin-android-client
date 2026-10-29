package com.flagsmith.entities

import com.google.gson.annotations.SerializedName

data class Flag(
    val feature: Feature,
    @SerializedName(value = "feature_state_value") val featureStateValue: Any?,
    val enabled: Boolean,
    /** The key of the multivariate option served to the identity, if any. */
    val variant: String? = null,
    val metadata: FlagMetadata? = null
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

data class FlagMetadata(
    /** The experiment running on the flag's feature, if any. */
    val experiment: Experiment? = null
)

data class Experiment(
    val id: Long,
    val name: String,
    /** Whether the identity is enrolled in the experiment. */
    @SerializedName(value = "in_experiment") val inExperiment: Boolean
)
