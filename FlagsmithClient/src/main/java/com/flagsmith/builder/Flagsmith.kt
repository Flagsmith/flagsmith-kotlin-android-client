package com.flagsmith.builder

import android.content.Context
import com.flagsmith.api.*
import com.flagsmith.interfaces.*
import com.flagsmith.response.*

class Flagsmith private constructor(
    val apiAuthToken: String?,
    val environmentKey: String?,
    val baseUrl: String = DEFAULT_BASE_URL,
    val context: Context?,
    private val enableAnalytics: Boolean = DEFAULT_ENABLE_ANALYTICS,
    private val analyticsFlushPeriod: Int = DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS
) {
    private var analytics: FlagsmithAnalytics? = null

    init {
        if (enableAnalytics && context != null) {
            this.analytics = FlagsmithAnalytics(this, context, analyticsFlushPeriod)
        }
        if (enableAnalytics && context == null) {
            throw IllegalArgumentException("Flagsmith requires a context to use the analytics feature")
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://edge.api.flagsmith.com/api/v1/"
        const val DEFAULT_ENABLE_ANALYTICS = true
        const val DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS = 10
    }

    fun getFeatureFlags(identity: String?, result: (Result<List<Flag>>) -> Unit) {
        if (identity != null) {
            GetIdentityFlagsAndTraits(this, identity, object : IIdentityFlagsAndTraitsResult {
                override fun success(response: IdentityFlagsAndTraits) {
                    result(Result.success(response.flags))
                }

                override fun failed(e: Exception) {
                    result(Result.failure(e))
                }
            })
        } else {
            GetFlags(this, object : IFlagArrayResult {
                override fun success(list: ArrayList<Flag>) {
                    result(Result.success(list))
                }

                override fun failed(str: String) {
                    result(Result.failure(IllegalStateException(str)))
                }
            })
        }
    }

    fun hasFeatureFlag(forFeatureId: String, identity: String? = null, result:(Result<Boolean>) -> Unit) {
        if (identity != null) {
            GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
                override fun success(response: IdentityFlagsAndTraits) {
                    val flag = response.flags.find { flag -> flag.feature.name == forFeatureId && flag.enabled }
                    analytics?.trackEvent(forFeatureId)
                    result(Result.success(flag != null))
                }

                override fun failed(e: Exception) {
                    result(Result.failure(e))
                }
            })
        } else {
            GetFlags(this, object : IFlagArrayResult {
                override fun success(list: ArrayList<Flag>) {
                    val found = list.find { flag -> flag.feature.name == forFeatureId }
                    val enabled = found?.enabled ?: false
                    result(Result.success(enabled))
                }

                override fun failed(str: String) {
                    result(Result.failure(IllegalStateException(str)))
                }
            })
        }
    }

    fun getValueForFeature(searchFeatureId: String, identity: String? = null, result: (Result<Any?>) -> Unit) {
        if (identity != null) {
            GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
                override fun success(response: IdentityFlagsAndTraits) {
                    val flag = response.flags.find { flag -> flag.feature.name == searchFeatureId && flag.enabled }
                    analytics?.trackEvent(searchFeatureId)
                    result(Result.success(flag?.featureStateValue))
                }

                override fun failed(e: Exception) {
                    result(Result.failure(e))
                }
            })
        } else {
            GetFlags(this, object : IFlagArrayResult {
                override fun success(list: ArrayList<Flag>) {
                    val found = list.find { flag -> flag.feature.name == searchFeatureId }
                    result(Result.success(found?.featureStateValue))
                }

                override fun failed(str: String) {
                    result(Result.failure(IllegalStateException(str)))
                }
            })
        }
    }

    fun getTrait(id: String, identity: String, result: (Result<Trait?>) -> Unit) {
        GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
            override fun success(response: IdentityFlagsAndTraits) {
                val trait = response.traits.find { it.key == id }
                result(Result.success(trait))
            }

            override fun failed(e: Exception) {
                result(Result.failure(e))
            }
        })
    }

    fun getTraits(identity: String, result: (Result<List<Trait>>) -> Unit) {
        GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
            override fun success(response: IdentityFlagsAndTraits) {
                result(Result.success(response.traits))
            }

            override fun failed(e: Exception) {
                result(Result.failure(e))
            }
        })
    }

    fun setTrait(trait: Trait, identity: String, result: (Result<TraitWithIdentity>) -> Unit) {
        SetTrait(this, trait, identity, object: ITraitUpdateResult {
            override fun success(response: TraitWithIdentity) {
                result(Result.success(response))
            }

            override fun failed(exception: Exception) {
                result(Result.failure(exception))
            }
        })
    }

    fun getIdentity(identity: String, result: (Result<IdentityFlagsAndTraits>) -> Unit){
        GetIdentityFlagsAndTraits(this, identity = identity, object: IIdentityFlagsAndTraitsResult {
            override fun success(response: IdentityFlagsAndTraits) {
                result(Result.success(response))
            }

            override fun failed(e: Exception) {
                result(Result.failure(e))
            }
        })
    }

    override fun toString(): String {
        return "Flagsmith(apiAuthToken=$apiAuthToken, environmentKey=$environmentKey, baseUrl='$baseUrl', context=$context, enableAnalytics=$enableAnalytics, analyticsFlushPeriod=$analyticsFlushPeriod, analytics=$analytics)"
    }

    data class Builder(
        var apiAuthToken: String? = null,
        var environmentKey: String? = null,
        var baseUrl: String? = null,
        var enableAnalytics: Boolean? = null,
        var analyticsFlushPeriod: Int? = null,
        var context: Context? = null
    ) {

        fun apiAuthToken(v: String) = apply { this.apiAuthToken = v }
        fun environmentKey(v: String) = apply { this.environmentKey = v }
        fun baseUrl(v: String) = apply { this.baseUrl = v }
        fun enableAnalytics(v: Boolean) = apply { this.enableAnalytics = v }
        fun analyticsFlushPeriod(v: Int) = apply { this.analyticsFlushPeriod = v }
        fun context(v: Context) = apply { this.context = v }

        fun build(): Flagsmith {
            return Flagsmith(apiAuthToken = apiAuthToken, environmentKey = environmentKey,
                baseUrl = baseUrl ?: DEFAULT_BASE_URL, enableAnalytics = enableAnalytics ?: DEFAULT_ENABLE_ANALYTICS,
                analyticsFlushPeriod = analyticsFlushPeriod ?: DEFAULT_ANALYTICS_FLUSH_PERIOD_SECONDS,
                context = context
            )
        }
    }



}