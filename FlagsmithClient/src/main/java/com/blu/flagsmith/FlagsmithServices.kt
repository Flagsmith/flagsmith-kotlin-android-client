package com.blu.flagsmith

import com.blu.flagsmith.entities.TraitWithIdentityModel
import com.flagsmith.entities.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FlagsmithServices {
    @GET("identities/")
    fun getIdentityFlagsAndTraits(@Query("identity") identity: String): IdentityFlagsAndTraitsModel

    @GET("flags/")
    fun getFlags(): List<FlagModel>

    @POST("traits/")
    fun postTraits(@Body trait: TraitWithIdentityModel): TraitWithIdentityModel

    @POST("analytics/flags/")
    fun postAnalytics(@Body eventMap: Map<String, Int?>): Any
}