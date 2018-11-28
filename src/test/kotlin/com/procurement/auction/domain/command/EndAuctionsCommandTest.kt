package com.procurement.auction.domain.command

import com.procurement.auction.AbstractBase
import com.procurement.auction.toJson
import com.procurement.auction.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class EndAuctionsCommandTest : AbstractBase() {
    @Test
    fun test() {
        val json = RESOURCES.load("json/command/end.json")
        val obj = mapper.toObject<EndAuctionsCommand>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
