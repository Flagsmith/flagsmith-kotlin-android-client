package com.flagsmith.entities

data class Identity(
    val identifier: String,
    val transient: Boolean? = false
)