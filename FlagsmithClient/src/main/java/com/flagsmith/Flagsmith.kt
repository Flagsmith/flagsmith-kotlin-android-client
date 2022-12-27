package com.flagsmith

import android.content.Context
import com.flagsmith.internal.FlagsmithApi
import com.flagsmith.internal.*
import com.flagsmith.entities.*
import com.github.kittinunf.fuel.Fuel
import kotlin.reflect.KProperty1
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
    private val baseUrl: String? = null,
    private val context: Context? = null,
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

    fun getFeatureFlags(identity: String? = null, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            getIdentityFlagsAndTraits(identity) { res ->
                result(res.map { it.flags })
            }
        } else {
            Fuel.request(FlagsmithApi.getFlags())
                .responseObject(FlagListDeserializer()) { _, _, res ->
                    result(res.convertToResult())
                }
        }
    }

    fun hasFeatureFlag(
        forFeatureId: String,
        identity: String? = null,
        result: (Result<Boolean>) -> Unit
    ) = getFeatureFlags(identity) { res ->
        result(res.map { flags ->
            val foundFlag =
                flags.find { flag -> flag.feature.name == forFeatureId && flag.enabled }
            analytics?.trackEvent(forFeatureId)
            foundFlag != null
        })
    }

    fun getValueForFeature(
        searchFeatureId: String,
        identity: String? = null,
        result: (Result<Any?>) -> Unit
    ) = getFeatureFlags(identity) { res ->
        result(res.map { flags ->
            val foundFlag =
                flags.find { flag -> flag.feature.name == searchFeatureId && flag.enabled }
            analytics?.trackEvent(searchFeatureId)
            foundFlag?.featureStateValue
        })
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
        Fuel.request(FlagsmithApi.setTrait(trait = trait, identity = identity))
            .responseObject(TraitWithIdentityDeserializer()) { _, _, res ->
                result(res.convertToResult())
            }
    }

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit) =
        getIdentityFlagsAndTraits(identity, result)

    private fun getIdentityFlagsAndTraits(
        identity: String,
        result: (Result<IdentityFlagsAndTraits>) -> Unit
    ) = Fuel.request(FlagsmithApi.getIdentityFlagsAndTraits(identity = identity))
        .responseObject(IdentityFlagsAndTraitsDeserializer()) { _, _, res ->
            result(res.convertToResult())
        }

    private fun <A, B : Exception> FuelResult<A, B>.convertToResult(): Result<A> =
        fold(
            success = { value -> Result.success(value) },
            failure = { err -> Result.failure(err) }
        )
}