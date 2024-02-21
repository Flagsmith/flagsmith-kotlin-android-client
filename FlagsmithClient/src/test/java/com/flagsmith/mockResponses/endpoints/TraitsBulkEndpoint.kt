package com.flagsmith.mockResponses.endpoints

import com.flagsmith.entities.Identity
import com.flagsmith.entities.Trait
import com.flagsmith.entities.TraitWithIdentity
import com.google.gson.Gson

data class TraitsBulkEndpoint(private val traits: List<Trait>, private val identity: String) :
    PostEndpoint<TraitWithIdentity>(
        path = "/traits/bulk/",
        body = Gson().toJson(
            traits.map {
                TraitWithIdentity(
                    key = it.key,
                    traitValue = it.traitValue,
                    identity = Identity(identity)
                )
            }
        ),
    )