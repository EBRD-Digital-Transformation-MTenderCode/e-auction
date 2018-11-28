package com.procurement.auction.domain.model.sign

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class SignSerializer : ValueObjectSerializer<Sign>() {
    companion object {
        fun serialize(sign: Sign) = sign.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(sign: Sign, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(SignSerializer.serialize(sign))
}