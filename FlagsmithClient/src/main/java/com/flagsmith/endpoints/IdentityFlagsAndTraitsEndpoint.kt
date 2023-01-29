package com.flagsmith.endpoints

import com.flagsmith.entities.IdentityFlagsAndTraits
import com.flagsmith.entities.IdentityFlagsAndTraitsDeserializer

data class IdentityFlagsAndTraitsEndpoint(private val identity: String) :
    GetEndpoint<IdentityFlagsAndTraits>(
        path = "/identities/",
        params = listOf("identifier" to identity),
        deserializer = IdentityFlagsAndTraitsDeserializer()
    )