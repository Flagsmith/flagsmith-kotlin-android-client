package com.flagsmith.internal

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import java.io.Reader
import java.lang.reflect.Type

interface Deserializer<Response : Any> : ResponseDeserializable<Response>
object EmptyDeserializer : Deserializer<Unit> {
    override fun deserialize(reader: Reader) = Unit
}

fun <T> Reader.fromJson(classType: Class<T>): T? =
    Gson().fromJson(this, classType)

fun <T> Reader.fromJson(type: Type): T? =
    Gson().fromJson(this, type)