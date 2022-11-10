package com.flagsmith.internal

import com.flagsmith.entities.Identity
import com.flagsmith.entities.Trait
import com.flagsmith.entities.TraitWithIdentity
import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.util.FuelRouting
import com.google.gson.Gson

sealed class FlagsmithApi: FuelRouting {
    class getIdentityFlagsAndTraits(val identity: String): FlagsmithApi()
    class getFlags(): FlagsmithApi()
    class setTrait(val trait: Trait, val identity: String): FlagsmithApi()
    class postAnalytics(val eventMap: Map<String, Int?>): FlagsmithApi()

    companion object {
        var environmentKey: String? = null
        var baseUrl: String? = null
    }

    override val basePath: String
        get() {
            if (baseUrl == null) {
                throw IllegalStateException("baseUrl not set in FlagsmithApi, check Flagsmith SDK initialization")
            } else {
                return baseUrl!!
            }
        }
    override val body: String?
        get() {
            return when(this) {
                is setTrait -> Gson().toJson(TraitWithIdentity(key = trait.key, value = trait.value, identity = Identity(identity)))
                is postAnalytics -> Gson().toJson(eventMap)
                else -> null
            }
        }

    override val bytes: ByteArray?
        get() = null

    override val headers: Map<String, HeaderValues>?
        get() {
            val headers = mutableMapOf<String, HeaderValues>("X-Environment-Key" to listOf(environmentKey ?: ""))
            if (method == Method.POST) {
                headers["Content-Type"] = listOf("application/json")
            }
            return headers
        }

    override val method: Method
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> Method.GET
                is getFlags -> Method.GET
                is setTrait -> Method.POST
                is postAnalytics -> Method.POST
            }
        }

    override val params: Parameters?
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> listOf("identifier" to this.identity)
                is setTrait -> listOf("identifier" to this.identity)
                else -> null
            }
        }

    override val path: String
        get() {
            return when(this) {
                is getIdentityFlagsAndTraits -> "/identities"
                is getFlags -> "/flags"
                is setTrait -> "/traits"
                is postAnalytics -> "/analytics/flags"
            }
        }
}