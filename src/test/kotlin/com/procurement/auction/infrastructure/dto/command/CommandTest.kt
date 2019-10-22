package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CommandTest : AbstractDTOTestBase<Command>(Command::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/command/command.json")
    }
}
