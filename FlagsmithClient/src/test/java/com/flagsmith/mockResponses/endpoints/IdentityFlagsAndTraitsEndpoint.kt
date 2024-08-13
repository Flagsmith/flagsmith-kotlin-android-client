package com.flagsmith.mockResponses.endpoints

import com.flagsmith.entities.IdentityFlagsAndTraits

data class IdentityFlagsAndTraitsEndpoint(private val identity: String, private val transient: Boolean = false) :
    GetEndpoint<IdentityFlagsAndTraits>(
        path = "/identities/",
        params = listOf("identifier" to identity, "transient" to transient),
    )