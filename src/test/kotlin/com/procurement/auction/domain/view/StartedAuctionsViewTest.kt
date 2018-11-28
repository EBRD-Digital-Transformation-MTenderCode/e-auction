package com.procurement.auction.domain.view

import com.procurement.auction.AbstractBase
import com.procurement.auction.toJson
import com.procurement.auction.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class StartedAuctionsViewTest : AbstractBase() {
    @Test
    fun test() {
        val json = RESOURCES.load("json/view/start.json")
        val obj = mapper.toObject<StartedAuctionsView>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
