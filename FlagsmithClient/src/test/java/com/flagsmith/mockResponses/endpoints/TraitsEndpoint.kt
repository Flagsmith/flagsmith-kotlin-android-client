package com.flagsmith.mockResponses.endpoints

import com.flagsmith.entities.IdentityAndTraits
import com.flagsmith.entities.Trait
import com.flagsmith.entities.TraitWithIdentity
import com.google.gson.Gson

data class TraitsEndpoint(private val trait: Trait, private val identity: String, private val transient: Boolean = false) :
    PostEndpoint<TraitWithIdentity>(
        path = "/identities/",
        body = Gson().toJson(
            IdentityAndTraits(
                identifier = identity,
                traits = listOf(trait),
                transient = transient,
            )
        ),
    )
