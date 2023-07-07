package com.flagsmith.mockResponses.endpoints

import com.flagsmith.entities.IdentityFlagsAndTraits

data class IdentityFlagsAndTraitsEndpoint(private val identity: String) :
    GetEndpoint<IdentityFlagsAndTraits>(
        path = "/identities/",
        params = listOf("identifier" to identity),
    )