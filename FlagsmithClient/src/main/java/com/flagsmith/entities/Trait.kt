package com.flagsmith.entities

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class Trait (
    val identifier: String? = null,
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val traitValue: Any,
    val transient: Boolean = false
) {

    constructor(key: String, value: String, transient: Boolean = false)
            : this(key = key, traitValue = value, transient = transient)

    constructor(key: String, value: Int, transient: Boolean = false)
            : this(key = key, traitValue = value, transient = transient)

    constructor(key: String, value: Double, transient: Boolean = false)
            : this(key = key, traitValue = value, transient = transient)

    constructor(key: String, value: Boolean, transient: Boolean = false)
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

class TraitSerializer : JsonSerializer<Trait> {
    override fun serialize(src: Trait, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        src.identifier?.let { jsonObject.addProperty("identifier", it) }
        jsonObject.addProperty("trait_key", src.key)
        jsonObject.addProperty("trait_value", src.traitValue.toString())
        if (src.transient) {
            jsonObject.addProperty("transient", true)
        }
        return jsonObject
    }
}

data class TraitWithIdentity (
    @SerializedName(value = "trait_key") val key: String,
    @SerializedName(value = "trait_value") val traitValue: Any,
    val identity: Identity,
    val transient: Boolean = false
) {
    constructor(key: String, value: String, identity: Identity, transient: Boolean = false)
            : this(key = key, traitValue = value, identity = identity, transient = transient)

    constructor(key: String, value: Int, identity: Identity, transient: Boolean = false)
            : this(key = key, traitValue = value, identity = identity, transient = transient)

    constructor(key: String, value: Double, identity: Identity, transient: Boolean = false)
            : this(key = key, traitValue = value, identity = identity, transient = transient)

    constructor(key: String, value: Boolean, identity: Identity, transient: Boolean = false)
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
