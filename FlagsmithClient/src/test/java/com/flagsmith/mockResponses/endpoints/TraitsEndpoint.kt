package com.flagsmith.mockResponses.endpoints

import com.flagsmith.entities.Identity
import com.flagsmith.entities.Trait
import com.flagsmith.entities.TraitWithIdentity
import com.google.gson.Gson

data class TraitsEndpoint(private val trait: Trait, private val identity: String) :
    PostEndpoint<TraitWithIdentity>(
        path = "/traits/",
        body = Gson().toJson(
            TraitWithIdentity(
                key = trait.key,
                traitValue = trait.traitValue,
                identity = Identity(identity)
            )
        ),
    )