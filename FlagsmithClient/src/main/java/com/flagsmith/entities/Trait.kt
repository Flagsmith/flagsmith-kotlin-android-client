package com.flagsmith.entities


import com.google.gson.annotations.SerializedName

data class Trait(
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val traitValue: Any
) {

    constructor(key: String, value: String) : this(null, key, value)
    constructor(key: String, value: Int) : this(null, key, value)
    constructor(key: String, value: Double) : this(null, key, value)
    constructor(key: String, value: Boolean) : this(null, key, value)

    @Deprecated("Use traitValue instead", ReplaceWith("traitValue"))
    val value: String?
        get() = stringValue

    val stringValue: String?
        get() = traitValue as? String

    val intValue: Int?
        get() = (traitValue as? Double)?.toInt()

    val doubleValue: Double?
        get() = traitValue as? Double

    val booleanValue: Boolean?
        get() = traitValue as? Boolean

}

data class TraitWithIdentity(
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val value: Any,
    val identity: Identity,
) {
    // Add a constructor that takes a string and sets the value to it
    constructor(key: String, value: String?, identity: Identity) : this(key, value as Any, identity)

    // Add a constructor that takes an int and sets the value to it
    constructor(key: String, value: Int, identity: Identity) : this(key, value as Any, identity)
}
