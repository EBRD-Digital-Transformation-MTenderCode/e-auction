package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.AbstractBase
import com.procurement.auction.toJson
import com.procurement.auction.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class StartAuctionsCommandTest : AbstractBase() {
    @Test
    fun test() {
        val json = RESOURCES.load("json/command/start.json")
        val obj = mapper.toObject<StartAuctionsCommand>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
