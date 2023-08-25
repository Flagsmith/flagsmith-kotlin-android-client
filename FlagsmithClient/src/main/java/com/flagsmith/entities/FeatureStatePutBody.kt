package com.flagsmith.entities

internal data class FeatureStatePutBody (
    val enabled: Boolean,
    val feature_state_value: Any?
)
