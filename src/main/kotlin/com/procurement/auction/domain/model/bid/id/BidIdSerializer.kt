package com.procurement.auction.domain.model.bid.id

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class BidIdSerializer : ValueObjectSerializer<BidId>() {
    companion object {
        fun serialize(bidId: BidId) = bidId.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(bidId: BidId, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(serialize(bidId))
}