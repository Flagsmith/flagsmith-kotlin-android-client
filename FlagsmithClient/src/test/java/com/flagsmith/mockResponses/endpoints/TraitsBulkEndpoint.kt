package com.flagsmith.mockResponses.endpoints

import com.flagsmith.entities.IdentityAndTraits
import com.flagsmith.entities.Trait
import com.flagsmith.entities.TraitWithIdentity
import com.google.gson.Gson

data class TraitsBulkEndpoint(private val traits: List<Trait>, private val identity: String) :
    PostEndpoint<TraitWithIdentity>(
        path = "/identities/",
        body = Gson().toJson(
            IdentityAndTraits(
                identifier = identity,
                traits = traits
            )
        ),
    )