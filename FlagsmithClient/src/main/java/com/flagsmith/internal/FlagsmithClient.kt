package com.flagsmith.internal

import com.flagsmith.endpoints.Endpoint
import com.flagsmith.endpoints.GetEndpoint
import com.flagsmith.endpoints.PostEndpoint
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.util.FuelRouting
import com.github.kittinunf.result.Result as FuelResult

class FlagsmithClient(
    private val baseUrl: String,
    environmentKey: String
) {
    val defaultHeaders = mapOf<String, HeaderValues>(
        "X-Environment-Key" to listOf(environmentKey),
    )

    fun <Response : Any> request(endpoint: Endpoint<Response>, handler: (Result<Response>) -> Unit) =
        Fuel.request(createRequest(endpoint))
            .responseObject(endpoint.deserializer) { _, _, res ->
                handler(convertToKotlinResult(res))
            }

    private fun <Response : Any> createRequest(endpoint: Endpoint<Response>): FuelRouting {
        return object : FuelRouting {
            override val basePath = baseUrl
            override val body: String? = endpoint.body
            override val bytes: ByteArray? = null
            override val headers: Map<String, HeaderValues> = defaultHeaders + endpoint.headers
            override val method: Method = when (endpoint) {
                is GetEndpoint -> Method.GET
                is PostEndpoint -> Method.POST
            }
            override val params: Parameters? = endpoint.params
            override val path: String = endpoint.path
        }
    }

    private fun <A, B : Exception> convertToKotlinResult(result: FuelResult<A, B>): Result<A> =
        result.fold(
            success = { value -> Result.success(value) },
            failure = { err -> Result.failure(err) }
        )
}