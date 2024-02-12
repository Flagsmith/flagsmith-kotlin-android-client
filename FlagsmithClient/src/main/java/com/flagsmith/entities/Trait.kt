package com.flagsmith.entities


import com.google.gson.annotations.SerializedName

data class Trait(
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val value: String
) {
    constructor(key: String, value: String) : this(null, key, value)
}

data class TraitWithIdentity(
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val value: Any,
    val identity: Identity,
) {
    // Add a constructor that takes a string and sets the value to it
    constructor(key: String, value: String, identity: Identity) : this(key, value as Any, identity)

    // Add a constructor that takes an int and sets the value to it
    constructor(key: String, value: Int, identity: Identity) : this(key, value as Any, identity)
}
