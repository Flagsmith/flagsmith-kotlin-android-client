package com.flagsmith.entities


import com.google.gson.annotations.SerializedName

data class TraitConfig(
    val value: TraitValue,
    val transient: Boolean? = false,
)

data class Trait (
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val traitValue: Any,
    val transient: Boolean? = false,
) {

    constructor(key: String, value: String)
            : this(key = key, traitValue = value)

    constructor(key: String, value: Int)
            : this(key = key, traitValue = value)

    constructor(key: String, value: Double)
            : this(key = key, traitValue = value)

    constructor(key: String, value: Boolean)
            : this(key = key, traitValue = value)

    // constructor(key: String, value: TrairConfig)
    //         : this(key = key, traitValue = value)

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

data class TraitWithIdentity (
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val traitValue: Any,
    val identity: Identity,
    val transient: Boolean? = false,
) {
    constructor(key: String, value: String, identity: Identity)
            : this(key = key, traitValue = value, identity = identity)

    constructor(key: String, value: Int, identity: Identity)
            : this(key = key, traitValue = value, identity = identity)

    constructor(key: String, value: Double, identity: Identity)
            : this(key = key, traitValue = value, identity = identity)

    constructor(key: String, value: Boolean, identity: Identity)
            : this(key = key, traitValue = value, identity = identity)

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
