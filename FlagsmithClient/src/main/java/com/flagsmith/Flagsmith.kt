package com.flagsmith

import android.content.Context
import android.util.Log
import com.flagsmith.entities.Flag
import com.flagsmith.entities.Identity
import com.flagsmith.entities.IdentityAndTraits
import com.flagsmith.entities.IdentityFlagsAndTraits
import com.flagsmith.entities.Trait
import com.flagsmith.entities.TraitWithIdentity
import com.flagsmith.internal.FlagsmithAnalytics
import com.flagsmith.internal.FlagsmithEventService
import com.flagsmith.internal.FlagsmithEventTimeTracker
import com.flagsmith.internal.FlagsmithRetrofitService
import com.flagsmith.internal.enqueueWithResult
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Cache
import java.io.IOException

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
    private val baseUrl: String = "https://edge.api.flagsmith.com/api/v1/",
    private val eventSourceBaseUrl: String = "https://realtime.flagsmith.com/",
    private val context: Context? = null,
    private val enableAnalytics: Boolean = DEFAULT_ENABLE_ANALYTICS,
    private val enableRealtimeUpdates: Boolean = false,
    private val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS,
    private val cacheConfig: FlagsmithCacheConfig = FlagsmithCacheConfig(),
    private val defaultFlags: List<Flag> = emptyList(),
    private val requestTimeoutSeconds: Long = 4L,
    private val readTimeoutSeconds: Long = 6L,
    private val writeTimeoutSeconds: Long = 6L,
    override var lastFlagFetchTime: Double = 0.0 // from FlagsmithEventTimeTracker
) : FlagsmithEventTimeTracker {
    private lateinit var retrofit: FlagsmithRetrofitService
    private var cache: Cache? = null
    private var lastUsedIdentity: String? = null
    private var analytics: FlagsmithAnalytics? = null

    private val eventService: FlagsmithEventService? =
        if (!enableRealtimeUpdates) null
        else FlagsmithEventService(eventSourceBaseUrl = eventSourceBaseUrl, environmentKey = environmentKey) { event ->
            if (event.isSuccess) {
                lastEventUpdate = event.getOrNull()?.updatedAt ?: lastEventUpdate

                // Check whether this event is anything new
                if (lastEventUpdate > lastFlagFetchTime) {
                    // First evict the cache otherwise we'll be stuck with the old values
                    cache?.evictAll()
                    lastFlagFetchTime = lastEventUpdate

                    // Now we can get the new values, which will automatically be emitted to the flagUpdateFlow
                    getFeatureFlags(lastUsedIdentity) { res ->
                        if (res.isFailure) {
                            Log.e(
                                "Flagsmith",
                                "Error getting flags in SSE stream: ${res.exceptionOrNull()}"
                            )
                        } else {
                            Log.i("Flagsmith", "Got flags due to SSE event: $event")
                        }
                    }
                }
            }
        }

    // The last time we got an event from the SSE stream or via the API
    private var lastEventUpdate: Double = 0.0

    /** Stream of flag updates from the SSE stream if enabled */
    val flagUpdateFlow = MutableStateFlow<List<Flag>>(listOf())

    init {
        if (cacheConfig.enableCache && context == null) {
            throw IllegalArgumentException("Flagsmith requires a context to use the cache feature")
        }
        val pair = FlagsmithRetrofitService.create<FlagsmithRetrofitService>(
            baseUrl = baseUrl, environmentKey = environmentKey, context = context, cacheConfig = cacheConfig,
            requestTimeoutSeconds = requestTimeoutSeconds, readTimeoutSeconds = readTimeoutSeconds,
            writeTimeoutSeconds = writeTimeoutSeconds, timeTracker = this, klass = FlagsmithRetrofitService::class.java)
        retrofit = pair.first
        cache = pair.second

        if (enableAnalytics) {
            if (context == null || context.applicationContext == null) {
                throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")
            }
            analytics = FlagsmithAnalytics(context, retrofit, analyticsFlushPeriod)
        }
    }

    companion object {
        const val DEFAULT_ENABLE_ANALYTICS = true
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
    }

    fun getFeatureFlags(identity: String? = null, traits: List<Trait>? = null, transient: Boolean = false, result: (Result<List<Flag>>) -> Unit) {
        // Save the last used identity as we'll refresh with this if we get update events
        lastUsedIdentity = identity

        if (identity != null) {
            if (traits != null) {
                retrofit.postTraits(IdentityAndTraits(identity, traits, transient)).enqueueWithResult(result = {
                    result(it.map { response -> response.flags })
                }).also { lastUsedIdentity = identity }
            } else {
                retrofit.getIdentityFlagsAndTraits(identity, transient).enqueueWithResult { res ->
                    flagUpdateFlow.tryEmit(res.getOrNull()?.flags ?: emptyList())
                    result(res.map { it.flags })
                }
            }
        } else {
            if (traits != null) {
                throw IllegalArgumentException("Cannot set traits without an identity");
            } else {
                retrofit.getFlags().enqueueWithResult(defaults = defaultFlags) { res ->
                    flagUpdateFlow.tryEmit(res.getOrNull() ?: emptyList())
                    result(res)
                }
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
        retrofit.getIdentityFlagsAndTraits(identity).enqueueWithResult { res ->
            result(res.map { value -> value.traits.find { it.key == id } })
        }.also { lastUsedIdentity = identity }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) =
        retrofit.getIdentityFlagsAndTraits(identity).enqueueWithResult { res ->
            result(res.map { it.traits })
        }.also { lastUsedIdentity = identity }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) =
        retrofit.postTraits(IdentityAndTraits(identity, listOf(trait)))
            .enqueueWithResult(result = {
                result(it.map { response -> TraitWithIdentity(
                    key = response.traits.first().key,
                    traitValue = response.traits.first().traitValue,
                    identity = Identity(identity)
                )})
            })

    fun setTraits(traits: List<Trait>, identity: String, result: (Result<List<TraitWithIdentity>>) -> Unit) {
        retrofit.postTraits(IdentityAndTraits(identity, traits)).enqueueWithResult(result = {
            result(it.map { response -> response.traits.map { trait ->
                TraitWithIdentity(
                    key = trait.key,
                    traitValue = trait.traitValue,
                    identity = Identity(identity)
                )
            }})
        })
    }

    fun getIdentity(identity: String, transient: Boolean = false, result: (Result<IdentityFlagsAndTraits>) -> Unit) =
        retrofit.getIdentityFlagsAndTraits(identity, transient).enqueueWithResult(defaults = null, result = result)
            .also { lastUsedIdentity = identity }

    fun clearCache() {
        try {
            cache?.evictAll()
        } catch (e: IOException) {
            Log.e("Flagsmith", "Error clearing cache", e)
        }
    }

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
    }.also { lastUsedIdentity = identity }
}
