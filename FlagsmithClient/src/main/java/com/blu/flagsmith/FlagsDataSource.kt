package com.blu.flagsmith

import com.blu.flagsmith.entities.TraitWithIdentityModel
import com.blu.flagsmith.util.ResultEntity
import com.flagsmith.entities.FlagModel
import com.flagsmith.entities.IdentityFlagsAndTraitsModel

interface FlagsDataSource {

    suspend fun getIdentityFlagsAndTraits(identity: String): ResultEntity<IdentityFlagsAndTraitsModel>

    suspend fun getFlags(): ResultEntity<List<FlagModel>>

    suspend fun postTraits(trait: TraitWithIdentityModel): ResultEntity<TraitWithIdentityModel>

    suspend fun postAnalytics(eventMap: Map<String, Int?>): ResultEntity<Any>
}