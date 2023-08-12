package com.blu.flagsmith

import com.blu.flagsmith.entities.TraitWithIdentityModel
import com.blu.flagsmith.util.BaseRemoteDataSource
import com.blu.flagsmith.util.ErrorHandler

class FlagsmithRemoteDataSource(
    private val retrofitHelper: FlagsmithServices,
    errorHandler: ErrorHandler
) : FlagsDataSource, BaseRemoteDataSource(errorHandler) {

    override suspend fun getIdentityFlagsAndTraits(identity: String) =
        safeRequest {
            retrofitHelper.getIdentityFlagsAndTraits(identity)
        }


    override suspend fun getFlags() = safeRequest {
        retrofitHelper.getFlags()
    }

    override suspend fun postTraits(trait: TraitWithIdentityModel) = safeRequest {
        retrofitHelper.postTraits(trait)
    }

    override suspend fun postAnalytics(eventMap: Map<String, Int?>) = safeRequest {
        retrofitHelper.postAnalytics(eventMap)
    }


}