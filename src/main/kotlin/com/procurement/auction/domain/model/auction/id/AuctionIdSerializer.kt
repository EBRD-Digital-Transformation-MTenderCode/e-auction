package com.procurement.auction.domain.model.auction.id

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class AuctionIdSerializer : ValueObjectSerializer<AuctionId>() {
    companion object {
        fun serialize(auctionId: AuctionId) = auctionId.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(auctionId: AuctionId, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(serialize(
            auctionId))
}