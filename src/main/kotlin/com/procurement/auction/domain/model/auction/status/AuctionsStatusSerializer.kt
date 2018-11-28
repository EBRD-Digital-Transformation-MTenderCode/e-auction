package com.procurement.auction.domain.model.auction.status

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class AuctionsStatusSerializer : ValueObjectSerializer<AuctionsStatus>() {
    companion object {
        fun serialize(auctionsStatus: AuctionsStatus) = auctionsStatus.id
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(auctionsStatus: AuctionsStatus, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeNumber(serialize(
            auctionsStatus))
    }
}