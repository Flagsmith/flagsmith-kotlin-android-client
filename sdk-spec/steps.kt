// Steps for the Flagsmith SDK behavioural spec: https://github.com/Flagsmith/sdk-spec
//
// Each function implements the Gherkin step in the comment above it.

import com.flagsmith.Flagsmith
import com.flagsmith.entities.Flag
import com.flagsmith.entities.Trait
import java.util.concurrent.CompletableFuture

/** A flag in the shape the spec expects. */
data class SpecFlag(val feature: String, val enabled: Boolean, val value: Any?)

fun Map<String, Any>.asTraits() = map { (key, value) -> Trait(key = key, traitValue = value) }

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

// Create the SDK client with events enabled
fun createClientWithEvents(environmentKey: String, apiUrl: String, eventsUrl: String) =
    Flagsmith(
        environmentKey = environmentKey,
        baseUrl = apiUrl,
        enableAnalytics = false,
        enableEvents = true,
        eventsBaseUrl = eventsUrl,
    )

// When the application gets the environment flags
fun getEnvironmentFlags(flagsmith: Flagsmith) =
    await { flagsmith.getFeatureFlags(result = it) }.asSpecFlags()

// When the application gets the flags for identity "{identifier}" with traits:
fun getIdentityFlags(flagsmith: Flagsmith, identifier: String, traits: Map<String, Any>) =
    await {
        flagsmith.getFeatureFlags(
            identity = identifier,
            traits = traits.asTraits(),
            result = it,
        )
    }.asSpecFlags()

// When the application gets the experiment flag "{feature}" for identity "{identifier}" with traits:
fun getExperimentFlag(flagsmith: Flagsmith, feature: String, identifier: String, traits: Map<String, Any>) =
    listOfNotNull(
        await<Flag?> {
            flagsmith.getExperimentFlag(feature, identity = identifier, traits = traits.asTraits(), result = it)
        }
    ).asSpecFlags()

// When the application tracks event "{event}" for identity "{identifier}" with value {value} and metadata:
fun trackEvent(flagsmith: Flagsmith, event: String, identifier: String, value: Any?, metadata: Map<String, Any?>) =
    flagsmith.trackEvent(event, identity = identifier, value = value, metadata = metadata)

// When the application tracks an exposure of feature "{feature}" for identity "{identifier}" with value {value} and metadata:
fun trackExposureEvent(
    flagsmith: Flagsmith,
    feature: String,
    identifier: String,
    value: Any?,
    metadata: Map<String, Any?>,
) = flagsmith.trackExposureEvent(feature, identity = identifier, value = value, metadata = metadata)
