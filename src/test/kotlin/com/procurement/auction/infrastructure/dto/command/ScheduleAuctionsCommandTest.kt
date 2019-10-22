package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class ScheduleAuctionsCommandTest : AbstractDTOTestBase<ScheduleAuctionsCommand>(ScheduleAuctionsCommand::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/command/schedule.json")
    }
}
