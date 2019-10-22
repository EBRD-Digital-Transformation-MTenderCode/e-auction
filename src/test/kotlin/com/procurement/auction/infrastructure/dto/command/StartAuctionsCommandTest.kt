package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class StartAuctionsCommandTest : AbstractDTOTestBase<StartAuctionsCommand>(StartAuctionsCommand::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/command/start.json")
    }
}
