package com.flagsmith

import android.content.Context
import com.flagsmith.internal.FlagsmithApi
import com.flagsmith.internal.*
import com.flagsmith.entities.*
import com.github.kittinunf.fuel.Fuel

class Flagsmith constructor(
    private val environmentKey: String,
    private val baseUrl: String? = null,
    val context: Context? = null,
    private val enableAnalytics: Boolean = DEFAULT_ENABLE_ANALYTICS,
    private val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS
) {
    private var analytics: FlagsmithAnalytics? = null

    init {
        if (enableAnalytics && context != null) {
            this.analytics = FlagsmithAnalytics(context, analyticsFlushPeriod)
        }
        if (enableAnalytics && context == null) {
            throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")
        }
        FlagsmithApi.baseUrl = baseUrl ?: "https://edge.api.flagsmith.com/api/v1"
        FlagsmithApi.environmentKey = environmentKey
    }

    companion object {
        const val DEFAULT_ENABLE_ANALYTICS = true
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
    }

    fun getFeatureFlags(identity: String?, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            Fuel.request(
                FlagsmithApi.getIdentityFlagsAndTraits(identity = identity))
                .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                    res.fold(
                        success = { value -> result(Result.success(value.flags)) },
                        failure = { err -> result(Result.failure(err)) }
                    )
                }
        } else {
            Fuel.request(FlagsmithApi.getFlags())
                .responseObject(FlagListDeserializer()) { _, _, res ->
                    res.fold(
                        success = { value -> result(Result.success(value)) },
                        failure = { err -> result(Result.failure(err)) }
                    )
                }
        }
    }

    fun hasFeatureFlag(forFeatureId: String, identity: String? = null, result:(Result<Boolean>) -> Unit) {
        getFeatureFlags(identity) { res ->
            res.fold(
                onSuccess = { flags ->
                    val foundFlag = flags.find { flag -> flag.feature.name == forFeatureId && flag.enabled }
                    analytics?.trackEvent(forFeatureId)
                    result(Result.success(foundFlag != null))
                },
                onFailure = { err -> result(Result.failure(err))}
            )
        }
    }

    fun getValueForFeature(searchFeatureId: String, identity: String? = null, result: (Result<Any?>) -> Unit) {
        getFeatureFlags(identity) { res ->
            res.fold(
                onSuccess = { flags ->
                    val foundFlag = flags.find { flag -> flag.feature.name == searchFeatureId && flag.enabled }
                    analytics?.trackEvent(searchFeatureId)
                    result(Result.success(foundFlag?.featureStateValue))
                },
                onFailure = { err -> result(Result.failure(err))}
            )
        }
    }

    fun getTrait(id: String, identity: String, result: (Result<Trait?>) -> Unit) {
        Fuel.request(
            FlagsmithApi.getIdentityFlagsAndTraits(identity = identity))
            .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                res.fold(
                    success = { value ->
                        val trait = value.traits.find { it.key == id }
                        result(Result.success(trait))
                    },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) {
        Fuel.request(
            FlagsmithApi.getIdentityFlagsAndTraits(identity = identity))
            .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                res.fold(
                    success = { value -> result(Result.success(value.traits)) },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) {
        Fuel.request(
            FlagsmithApi.setTrait(trait = trait, identity = identity))
            .responseObject(TraitWithIdentityDeserializer()) { _, _, res ->
                res.fold(
                    success = { value -> result(Result.success(value)) },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit){
        Fuel.request(
            FlagsmithApi.getIdentityFlagsAndTraits(identity = identity))
            .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
                res.fold(
                    success = { value ->
                        result(Result.success(value))
                    },
                    failure = { err -> result(Result.failure(err)) }
                )
            }
    }
}