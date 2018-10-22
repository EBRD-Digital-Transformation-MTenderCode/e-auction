package com.procurement.auction.entity.auction.start

import com.procurement.auction.AbstractBase
import com.procurement.auction.entity.auction.StartedAuctions
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class BiddersTest : AbstractBase() {
    @Test
    fun test() {
        val json = RESOURCES.load("json/auction/start/bidders.json")
        val obj = mapper.toObject<StartedAuctions.Bidders>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
