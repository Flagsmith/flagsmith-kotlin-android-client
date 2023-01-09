package com.flagsmith.endpoints

data class IdentityFlagsAndTraitsEndpoint(private val identity: String) :
    FlagsmithGetEndpoint(path = "/identities", params = listOf("identifier" to identity))