package com.procurement.auction.domain.model.lotId

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class LotIdSerializer : ValueObjectSerializer<LotId>() {
    companion object {
        fun serialize(lotId: LotId) = lotId.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(lotId: LotId, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(LotIdSerializer.serialize(lotId))
    }
}