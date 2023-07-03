package com.flagsmith.internal

import com.flagsmith.endpoints.Endpoint
import com.flagsmith.endpoints.GetEndpoint
import com.flagsmith.endpoints.PostEndpoint
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.util.FuelRouting
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.Fetcher
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
//            .timeout(5000) // 5 seconds
//            .timeoutRead(5000)
            .responseObject(endpoint.deserializer) { _, _, res ->
                handler(convertToKotlinResult(res))
            }

    fun <Response: Any> fetcher(endpoint: Endpoint<Response>,
                                convertible: Fuse.DataConvertible<Response>): Fetcher<Response> =
        EndpointFetcher(
            convertible = convertible,
            endpoint = endpoint,
            routing = createRequest(endpoint)
        )

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

    private class EndpointFetcher<Response: Any>(
        private val convertible: Fuse.DataConvertible<Response>,
        private val endpoint: Endpoint<Response>,
        private val routing: FuelRouting
    ) : Fetcher<Response>, Fuse.DataConvertible<Response> by convertible {

        override val key: String = endpoint.path + endpoint.params
        override fun fetch(): com.github.kittinunf.result.Result<Response, Exception> {
            return Fuel.request(routing).responseObject(endpoint.deserializer).third
        }
    }
}