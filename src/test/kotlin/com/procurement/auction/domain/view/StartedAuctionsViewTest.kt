package com.procurement.auction.domain.view

import com.procurement.auction.AbstractBase
import com.procurement.auction.toJson
import com.procurement.auction.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class StartedAuctionsViewTest : AbstractBase() {
    @Test
    fun auctionStartedTrue() {
        val json = RESOURCES.load("json/view/start_true.json")
        val obj = mapper.toObject<StartedAuctionsView>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }

    @Test
    fun auctionStartedFalse() {
        val json = RESOURCES.load("json/view/start_false.json")
        val obj = mapper.toObject<StartedAuctionsView>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
