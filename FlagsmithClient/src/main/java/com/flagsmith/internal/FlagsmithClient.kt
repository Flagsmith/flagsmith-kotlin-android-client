package com.flagsmith.internal

import com.flagsmith.endpoints.FlagsmithEndpoint
import com.flagsmith.endpoints.FlagsmithGetEndpoint
import com.flagsmith.endpoints.FlagsmithPostEndpoint
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.util.FuelRouting

class FlagsmithClient(
    private val baseUrl: String,
    environmentKey: String
) {
    val defaultHeaders = mutableMapOf<String, HeaderValues>(
        "X-Environment-Key" to listOf(environmentKey),
    )

    fun request(endpoint: FlagsmithEndpoint): Request =
        Fuel.request(createRequest(endpoint))

    private fun createRequest(endpoint: FlagsmithEndpoint): FuelRouting {
        return object : FuelRouting {
            override val basePath = baseUrl
            override val body: String? = endpoint.body
            override val bytes: ByteArray? = null
            override val headers: Map<String, HeaderValues> = defaultHeaders + endpoint.headers
            override val method: Method = when (endpoint) {
                is FlagsmithGetEndpoint -> Method.GET
                is FlagsmithPostEndpoint -> Method.POST
            }
            override val params: Parameters? = endpoint.params
            override val path: String = endpoint.path
        }
    }
}