package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.AbstractBase
import com.procurement.auction.toJson
import com.procurement.auction.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CommandTest : AbstractBase() {
    @Test
    fun command() {
        val json = RESOURCES.load("json/command/command.json")
        val obj = mapper.toObject<Command>(json)
        assertNotNull(obj)

        val jsonFromObj = mapper.toJson(obj)
        assertEquals(json, jsonFromObj)
    }
}
