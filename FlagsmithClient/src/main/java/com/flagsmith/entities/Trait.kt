package com.flagsmith.entities


import com.google.gson.annotations.SerializedName

data class Trait constructor(
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val traitValue: Any
) {

    constructor(key: String, value: String)
            : this(key = key, traitValue = value)

    constructor(key: String, value: Int)
            : this(key = key, traitValue = value)

    constructor(key: String, value: Double)
            : this(key = key, traitValue = value)

    constructor(key: String, value: Boolean)
            : this(key = key, traitValue = value)

    @Deprecated("Use traitValue instead or one of the type-safe getters", ReplaceWith("traitValue"))
    val value: String
        get()  { return traitValue as? String ?: traitValue.toString() }

    val stringValue: String?
        get() = traitValue as? String

    val intValue: Int?
        get()  {
            return when (traitValue) {
                is Int -> traitValue
                is Double -> traitValue.toInt()
                else -> null
            }
        }

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
