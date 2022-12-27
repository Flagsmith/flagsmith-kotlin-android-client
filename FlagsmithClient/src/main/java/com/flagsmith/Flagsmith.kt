package com.flagsmith

import android.content.Context
import com.flagsmith.internal.FlagsmithApi
import com.flagsmith.internal.*
import com.flagsmith.entities.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result as FuelResult

/**
 * Flagsmith
 *
 * The main interface to all of the Flagsmith functionality
 *
 * @property environmentKey Take this API key from the Flagsmith dashboard and pass here
 * @property baseUrl By default we'll connect to the Flagsmith backend, but if you self-host you can configure here
 * @property context The current context is required to use the Flagsmith Analytics functionality
 * @property enableAnalytics Enable analytics - default true
 * @property analyticsFlushPeriod The period in seconds between attempts by the Flagsmith SDK to push analytic events to the server
 * @constructor Create empty Flagsmith
 */
class Flagsmith constructor(
    private val environmentKey: String,
    private val baseUrl: String = "https://edge.api.flagsmith.com/api/v1",
    private val context: Context? = null,
    private val enableAnalytics: Boolean = DEFAULT_ENABLE_ANALYTICS,
    private val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS
) {
    private val analytics: FlagsmithAnalytics? =
        if (!enableAnalytics) null
        else if (context != null) FlagsmithAnalytics(context, analyticsFlushPeriod)
        else throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")

    init {
        FlagsmithApi.baseUrl = baseUrl
        FlagsmithApi.environmentKey = environmentKey
    }

    companion object {
        const val DEFAULT_ENABLE_ANALYTICS = true
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
    }

    fun getFeatureFlags(identity: String? = null, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            getIdentityFlagsAndTraits(identity) { res ->
                result(res.map { it.flags })
            }
        } else {
            Fuel.request(FlagsmithApi.GetFlags)
                .responseObject(FlagListDeserializer()) { _, _, res ->
                    result(res.convertToResult())
                }
        }
    }

    fun hasFeatureFlag(
        featureId: String,
        identity: String? = null,
        result: (Result<Boolean>) -> Unit
    ) = getFeatureFlag(featureId, identity) { res ->
        result(res.map { flag -> flag != null })
    }

    fun getValueForFeature(
        featureId: String,
        identity: String? = null,
        result: (Result<Any?>) -> Unit
    ) = getFeatureFlag(featureId, identity) { res ->
        result(res.map { flag -> flag?.featureStateValue })
    }

    fun getTrait(id: String, identity: String, result: (Result<Trait?>) -> Unit) =
        getIdentityFlagsAndTraits(identity) { res ->
            result(res.map { value -> value.traits.find { it.key == id } })
        }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) =
        getIdentityFlagsAndTraits(identity) { res ->
            result(res.map { it.traits })
        }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) {
        Fuel.request(FlagsmithApi.SetTrait(trait = trait, identity = identity))
            .responseObject(TraitWithIdentityDeserializer()) { _, _, res ->
                result(res.convertToResult())
            }
    }

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit) =
        getIdentityFlagsAndTraits(identity, result)

    private fun getFeatureFlag(
        featureId: String,
        identity: String?,
        result: (Result<Flag?>) -> Unit
    ) = getFeatureFlags(identity) { res ->
        result(res.map { flags ->
            val foundFlag = flags.find { flag -> flag.feature.name == featureId && flag.enabled }
            analytics?.trackEvent(featureId)
            foundFlag
        })
    }

    private fun getIdentityFlagsAndTraits(
        identity: String,
        result: (Result<IdentityFlagsAndTraits>) -> Unit
    ) = Fuel.request(FlagsmithApi.GetIdentityFlagsAndTraits(identity = identity))
        .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
            result(res.convertToResult())
        }

    private fun <A, B : Exception> FuelResult<A, B>.convertToResult(): Result<A> =
        fold(
            success = { value -> Result.success(value) },
            failure = { err -> Result.failure(err) }
        )
}