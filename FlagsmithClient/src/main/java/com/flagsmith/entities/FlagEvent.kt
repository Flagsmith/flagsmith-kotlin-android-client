package com.flagsmith.entities

import com.google.gson.annotations.SerializedName

internal data class FlagEvent (
    @SerializedName(value = "updated_at") val updatedAt: Double
)
