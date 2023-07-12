package com.flagsmith

import android.content.Context
import com.flagsmith.entities.*
import com.flagsmith.internal.FlagsmithAnalytics
import com.flagsmith.internal.FlagsmithRetrofitService
import com.flagsmith.internal.enqueueWithResult

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
    private val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS,
    private val cacheConfig: FlagsmithCacheConfig = FlagsmithCacheConfig(),
    private val defaultFlags: List<Flag> = emptyList(),
    private val requestTimeoutSeconds: Long = 4L,
    private val readTimeoutSeconds: Long = 6L,
    private val writeTimeoutSeconds: Long = 6L
) {
    private val retrofit: FlagsmithRetrofitService = FlagsmithRetrofitService.create(
        baseUrl = baseUrl, environmentKey = environmentKey, context = context, cacheConfig = cacheConfig,
        requestTimeoutSeconds = requestTimeoutSeconds, readTimeoutSeconds = readTimeoutSeconds, writeTimeoutSeconds = writeTimeoutSeconds)
    private val analytics: FlagsmithAnalytics? =
        if (!enableAnalytics) null
        else if (context != null) FlagsmithAnalytics(context, retrofit, analyticsFlushPeriod)
        else throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")

    init {
        if (cacheConfig.enableCache && context == null) {
            throw IllegalArgumentException("Flagsmith requires a context to use the cache feature")
        }
    }

    companion object {
        const val DEFAULT_ENABLE_ANALYTICS = true
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
    }

    fun getFeatureFlags(identity: String? = null, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            retrofit.getIdentityFlagsAndTraits(identity).enqueueWithResult { res ->
                result(res.map { it.flags })
            }
        } else {
            retrofit.getFlags().enqueueWithResult(defaults = defaultFlags, result = result)
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
        retrofit.getIdentityFlagsAndTraits(identity).enqueueWithResult { res ->
            result(res.map { value -> value.traits.find { it.key == id } })
        }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) =
        retrofit.getIdentityFlagsAndTraits(identity).enqueueWithResult { res ->
            result(res.map { it.traits })
        }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) =
        retrofit.postTraits(TraitWithIdentity(trait.key, trait.value, Identity(identity))).enqueueWithResult(result = result)

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit) =
        retrofit.getIdentityFlagsAndTraits(identity).enqueueWithResult(defaults = null, result = result)

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

}