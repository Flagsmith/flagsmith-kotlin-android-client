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

    @Deprecated("Use traitValue instead or one of the type-safe getters", ReplaceWith("traitValue"))
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
    val stringValue: String?
        get() = value as? String

    val intValue: Int?
        get() = (value as? Double)?.toInt()

    val doubleValue: Double?
        get() = value as? Double

    val booleanValue: Boolean?
        get() = value as? Boolean

}
