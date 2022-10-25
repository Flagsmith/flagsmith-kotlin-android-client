package com.flagsmith

import com.flagsmith.builder.Flagsmith
import com.flagsmith.response.Flag
import com.flagsmith.response.IdentityFlagsAndTraits
import com.flagsmith.response.Trait
import com.flagsmith.response.TraitWithIdentity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Flagsmith.hasFeatureFlagSync(forFeatureId: String, identity: String? = null): Result<Boolean>
    = suspendCoroutine { cont -> this.hasFeatureFlag(forFeatureId, identity = identity) { cont.resume(it) } }

suspend fun Flagsmith.getFeatureFlagsSync(identity: String? = null) : Result<List<Flag>>
    = suspendCoroutine { cont -> this.getFeatureFlags(identity = identity) { cont.resume(it) } }

suspend fun Flagsmith.getValueForFeatureSync(forFeatureId: String, identity: String? = null): Result<Any?>
    = suspendCoroutine { cont -> this.getValueForFeature(forFeatureId, identity = identity) { cont.resume(it) } }

suspend fun Flagsmith.getTraitsSync(identity: String): Result<List<Trait>>
    = suspendCoroutine { cont -> this.getTraits(identity) { cont.resume(it) } }

suspend fun Flagsmith.getTraitSync(id: String, identity: String): Result<Trait?>
    = suspendCoroutine { cont -> this.getTrait(id, identity) { cont.resume(it)} }

suspend fun Flagsmith.setTraitSync(trait: Trait, identity: String) : Result<TraitWithIdentity>
    = suspendCoroutine { cont -> this.setTrait(trait, identity) { cont.resume(it) } }

suspend fun Flagsmith.getIdentitySync(identity: String): Result<IdentityFlagsAndTraits>
    = suspendCoroutine { cont -> this.getIdentity(identity) { cont.resume(it) } }

