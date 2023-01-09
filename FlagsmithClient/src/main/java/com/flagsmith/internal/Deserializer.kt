package com.flagsmith.internal

import com.github.kittinunf.fuel.core.ResponseDeserializable
import java.io.Reader

interface Deserializer<Response : Any> : ResponseDeserializable<Response>
object EmptyDeserializer : Deserializer<Unit> {
    override fun deserialize(reader: Reader) = Unit
}