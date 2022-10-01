package com.flagsmith.response

data class ResponseTraits (
    val traits: ArrayList<Trait>
)

data class Trait (
    val id: Long,
    val trait_key: String,
    val trait_value: String
)

/**
 * Example
"traits": [
{
"id": 70489069,
"trait_key": "tempor_laboris",
"trait_value": "id sit aliquip vo"
}
]
 */