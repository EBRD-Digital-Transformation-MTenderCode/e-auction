package com.procurement.auction.domain.model.platformId

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class PlatformIdSerializer : ValueObjectSerializer<PlatformId>() {
    companion object {
        fun serialize(platformId: PlatformId) = platformId.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(platformId: PlatformId, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(PlatformIdSerializer.serialize(platformId))
}