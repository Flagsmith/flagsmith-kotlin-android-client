package com.flagsmith.entities

import com.blu.flagsmith.entities.FeatureModel
import com.google.gson.annotations.SerializedName

data class FlagModel(
    val feature: FeatureModel,
    @SerializedName(value = "feature_state_value") val featureStateValue: Any?,
    val enabled: Boolean
)
