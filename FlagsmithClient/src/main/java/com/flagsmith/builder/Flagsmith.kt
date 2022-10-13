package com.flagsmith.builder

import com.flagsmith.api.*
import com.flagsmith.interfaces.*

class Flagsmith private constructor(
    val tokenApiKey: String?,
    val environmentKey: String?,
    val identity: String?
) {

    override fun toString(): String {
        return "tokenApi: $tokenApiKey /environmentId: $environmentKey /identifierUser: $identity"
    }


    fun getFeatureFlags(finish: IFlagArrayResult) {
        Flag(this, finish)
    }

    fun hasFeatureFlag(searchFeatureId: String, finish: IFlagSingle) {
        Feature(this, searchFeatureId, finish)
    }


    fun getTrait(finish: ITraitArrayResult) {
        Trait(this, finish)
    }

    fun getTraits(finish: ITraitArrayResult){
        Trait(this, finish)
    }

    fun setTrait(key: String, value: String, finish: ITraitUpdate) {
        Identity(this, key, value, finish)
    }
//
//    fun getIdentity (finish: IIdentity){
//        Identity(this, finish)
//    }
//
//    fun enableAnalytics(analytics: FlagsmithAnalytics) {
//        analytics(this, )
//    }


    data class Builder(
        var tokenApi: String? = null,
        var environmentKey: String? = null,
        var identity: String? = null
    ) {

        fun tokenApi(v: String) = apply { this.tokenApi = v }
        fun environmentId(v: String) = apply { this.environmentKey = v }
        fun identity(v: String) = apply { this.identity = v }


        fun build(): Flagsmith {
            return Flagsmith(tokenApi, environmentKey, identity)
        }

    }
}