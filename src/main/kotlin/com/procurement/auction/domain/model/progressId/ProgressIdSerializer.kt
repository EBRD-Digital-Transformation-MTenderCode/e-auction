package com.procurement.auction.domain.model.progressId

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class ProgressIdSerializer : ValueObjectSerializer<ProgressId>() {
    companion object {
        fun serialize(progressId: ProgressId) = progressId.value
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(progressId: ProgressId, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(ProgressIdSerializer.serialize(progressId))
}