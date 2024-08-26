package com.flagsmith.entities


import com.google.gson.annotations.SerializedName


data class Trait (
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val traitValue: Any,
    val transient: Boolean? = null
) {

    constructor(key: String, value: String, transient: Boolean? = null)
            : this(key = key, traitValue = value, transient = transient)

    constructor(key: String, value: Int, transient: Boolean? = null)
            : this(key = key, traitValue = value, transient = transient)

    constructor(key: String, value: Double, transient: Boolean? = null)
            : this(key = key, traitValue = value, transient = transient)

    constructor(key: String, value: Boolean, transient: Boolean? = null)
            : this(key = key, traitValue = value, transient = transient)

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
    val transient: Boolean? = null
) {
    constructor(key: String, value: String, identity: Identity, transient: Boolean? = null)
            : this(key = key, traitValue = value, identity = identity, transient = transient)

    constructor(key: String, value: Int, identity: Identity, transient: Boolean? = null)
            : this(key = key, traitValue = value, identity = identity, transient = transient)

    constructor(key: String, value: Double, identity: Identity, transient: Boolean? = null)
            : this(key = key, traitValue = value, identity = identity, transient = transient)

    constructor(key: String, value: Boolean, identity: Identity, transient: Boolean? = null)
            : this(key = key, traitValue = value, identity = identity, transient = transient)

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
