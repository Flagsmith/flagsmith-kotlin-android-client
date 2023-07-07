package com.flagsmith.mockResponses.endpoints

//import com.flagsmith.internal.Deserializer

sealed interface Endpoint<Response : Any> {
    val body: String?
    val path: String
    val params: List<Pair<String, Any?>>?
    val headers: Map<String, Collection<String>>
}

sealed class GetEndpoint<Response : Any>(
    final override val path: String,
    final override val params: List<Pair<String, Any?>> = emptyList(),
    final override val headers: Map<String, Collection<String>> = emptyMap(),
) : Endpoint<Response> {
    final override val body: String? = null
}

sealed class PostEndpoint<Response : Any>(
    final override val path: String,
    final override val body: String,
    final override val params: List<Pair<String, Any?>> = emptyList(),
    headers: Map<String, Collection<String>> = emptyMap(),
) : Endpoint<Response> {
    final override val headers: Map<String, Collection<String>> =
        headers + mapOf("Content-Type" to listOf("application/json"))
}