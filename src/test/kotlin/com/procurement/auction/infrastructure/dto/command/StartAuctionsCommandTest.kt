package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class StartAuctionsCommandTest : AbstractDTOTestBase<StartAuctionsCommand>(StartAuctionsCommand::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/command/start_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/command/start_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/command/start_required_2.json")
    }
}
