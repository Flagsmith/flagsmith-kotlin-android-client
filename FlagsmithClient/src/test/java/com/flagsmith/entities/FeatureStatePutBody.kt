package com.flagsmith.entities

data class FeatureStatePutBody (
    val enabled: Boolean,
    val feature_state_value: Any?
)
