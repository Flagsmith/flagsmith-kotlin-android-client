package com.blu.flagsmith

import android.content.Context
import com.blu.flagsmith.entities.TraitWithIdentityModel
import com.blu.injection.Injector.bluFlagsmith
import com.flagsmith.entities.FlagModel
import com.flagsmith.entities.IdentityFlagsAndTraitsModel
import com.flagsmith.entities.IdentityModel
import com.flagsmith.entities.Trait
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject

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

class BluFlagsmith constructor(
    val environmentKey: String,
    val baseUrl: String = "https://edge.api.flagsmith.com/api/v1",
    val context: Context? = null,
    val enableAnalytics: Boolean = ANALYTICS_IS_ENABLE,
    val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS,
    val cacheConfig: FlagsmithCacheConfigModel = FlagsmithCacheConfigModel(),
    val defaultFlags: List<FlagModel> = emptyList(),
    val requestTimeoutSeconds: Long = 4L,
    val readTimeoutSeconds: Long = 6L,
    val writeTimeoutSeconds: Long = 6L
) {
    private val retrofit: FlagsmithServices by inject(this::class.java)

    private val analytics: BluFlagsmithAnalytics? =
        if (!enableAnalytics) null
        else if (context != null) BluFlagsmithAnalytics(context, retrofit, analyticsFlushPeriod)
        else throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")


    init {
        if (cacheConfig.enableCache && context == null) {
            throw IllegalArgumentException("Flagsmith requires a context to use the cache feature")
        }
        if (context != null) {
            startKoin {
                androidContext(context)
                modules(bluFlagsmith) // Define your Koin modules here
            }
        } else throw IllegalArgumentException("Flagsmith requires a context")
    }

    companion object {
        const val ANALYTICS_IS_ENABLE = false
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
    }

    fun getFeatureFlags(identity: String? = null): IdentityFlagsAndTraitsModel =
        if (identity != null) {
            retrofit.getIdentityFlagsAndTraits(identity)
        } else {
            throw IllegalArgumentException("Call getFlags if you cant set Identity")
        }

    fun getFlags(): List<FlagModel> =
        retrofit.getFlags()


    fun hasFeatureFlag(
        featureId: String,
        identity: String? = null
    ) = getFeatureFlag(featureId, identity)

    fun getValueForFeature(
        featureId: String,
        identity: String? = null
    ) = getFeatureFlag(featureId, identity)

    fun getTrait(id: String, identity: String) =
        retrofit.getIdentityFlagsAndTraits(identity)

    fun getTraits(identity: String) =
        retrofit.getIdentityFlagsAndTraits(identity)

    fun setTrait(trait: Trait, identity: String) =
        retrofit.postTraits(TraitWithIdentityModel(trait.key, trait.value, IdentityModel(identity)))

    fun getIdentity(identity: String) =
        retrofit.getIdentityFlagsAndTraits(identity)

    private fun getFeatureFlag(
        featureId: String,
        identity: String?
    ): IdentityFlagsAndTraitsModel = getFeatureFlags(identity).apply {
        flags.find { flag -> flag.feature.name == featureId && flag.enabled }
        analytics?.trackEvent(featureId)
    }

}