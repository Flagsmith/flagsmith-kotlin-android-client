package com.flagsmith.response

data class ResponseTraits(
    val traits: ArrayList<Trait>
)

data class Trait(
    val id: Long,
    val trait_key: String,
    val trait_value: String
)