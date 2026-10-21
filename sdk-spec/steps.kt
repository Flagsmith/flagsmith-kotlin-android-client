// Steps for the Flagsmith SDK behavioural spec: https://github.com/Flagsmith/sdk-spec
//
// Each function implements the Gherkin step in the comment above it.

import com.flagsmith.Flagsmith
import com.flagsmith.entities.Flag
import com.flagsmith.entities.Trait
import java.util.concurrent.CompletableFuture

/** A flag in the shape the spec expects. */
data class SpecFlag(val feature: String, val enabled: Boolean, val value: Any?)

fun List<Flag>.asSpecFlags() = map { SpecFlag(it.feature.name, it.enabled, it.featureStateValue) }

/** Blocks until the SDK calls back with its result. */
fun <T> await(call: ((Result<T>) -> Unit) -> Unit): T {
    val result = CompletableFuture<Result<T>>()
    call { result.complete(it) }
    return result.get().getOrThrow()
}

// Create the SDK client
fun createClient(environmentKey: String, apiUrl: String) =
    // Analytics requires an Android Context.
    Flagsmith(environmentKey = environmentKey, baseUrl = apiUrl, enableAnalytics = false)

// When the application gets the environment flags
fun getEnvironmentFlags(flagsmith: Flagsmith) =
    await { flagsmith.getFeatureFlags(result = it) }.asSpecFlags()

// When the application gets the flags for identity "{identifier}" with traits:
fun getIdentityFlags(flagsmith: Flagsmith, identifier: String, traits: Map<String, Any>) =
    await {
        flagsmith.getFeatureFlags(
            identity = identifier,
            traits = traits.map { (key, value) -> Trait(key = key, traitValue = value) },
            result = it,
        )
    }.asSpecFlags()
