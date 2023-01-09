package com.flagsmith.endpoints

sealed interface FlagsmithEndpoint {
    val body: String?
    val path: String
    val params: List<Pair<String, Any?>>?
    val headers: Map<String, Collection<String>>
}

sealed class FlagsmithGetEndpoint(
    final override val path: String,
    final override val params: List<Pair<String, Any?>> = emptyList(),
    final override val headers: Map<String, Collection<String>> = emptyMap()
) : FlagsmithEndpoint {
    final override val body: String? = null
}

sealed class FlagsmithPostEndpoint(
    final override val path: String,
    final override val body: String,
    final override val params: List<Pair<String, Any?>> = emptyList(),
    headers: Map<String, Collection<String>> = emptyMap()
) : FlagsmithEndpoint {
    final override val headers: Map<String, Collection<String>> =
        headers + mapOf("Content-Type" to listOf("application/json"))
}