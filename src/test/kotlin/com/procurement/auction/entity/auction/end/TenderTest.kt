package com.procurement.auction.entity.auction.end

import com.procurement.auction.AbstractBase
import com.procurement.auction.entity.auction.EndedAuctions
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TenderTest : AbstractBase() {
    @Test
    fun test() {
        val json = RESOURCES.load("json/auction/end/tender.json")
        val obj = mapper.toObject<EndedAuctions.Tender>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
