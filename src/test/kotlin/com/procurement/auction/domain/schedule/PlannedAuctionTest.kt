package com.procurement.auction.domain.schedule

import com.procurement.auction.AbstractBase
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class PlannedAuctionTest : AbstractBase() {
    @Test
    fun test() {
        val json = RESOURCES.load("json/schedule/auction_info.json")
        val obj = mapper.toObject<PlannedAuction>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
