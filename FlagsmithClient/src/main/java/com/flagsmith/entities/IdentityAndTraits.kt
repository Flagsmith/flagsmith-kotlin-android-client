package com.flagsmith.entities

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class IdentityAndTraits(
    @SerializedName(value = "identifier") val identifier: String,
    @SerializedName(value = "traits") val traits: List<Trait>,
    @SerializedName(value = "transient") val transient: Boolean? = null
)

class IdentityAndTraitsSerializer : JsonSerializer<IdentityAndTraits> {
    override fun serialize(src: IdentityAndTraits, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        // Create a JsonObject with all fields except transient
        val jsonObject = JsonObject()
        jsonObject.addProperty("identifier", src.identifier)
        jsonObject.add("traits", context.serialize(src.traits))
        
        // Only add transient if it's true
        if (src.transient == true) {
            jsonObject.addProperty("transient", true)
        }
        
        return jsonObject
    }
}
