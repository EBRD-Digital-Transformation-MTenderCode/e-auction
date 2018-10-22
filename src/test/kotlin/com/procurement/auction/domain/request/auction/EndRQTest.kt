package com.procurement.auction.domain.request.auction

import com.procurement.auction.AbstractBase
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class EndRQTest : AbstractBase() {
    @Test
    fun test() {
        val json = RESOURCES.load("json/auction/end/request.json")
        val obj = mapper.toObject<EndRQ>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
