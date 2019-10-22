package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CancelAuctionsCommandTest : AbstractDTOTestBase<CancelAuctionsCommand>(CancelAuctionsCommand::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/command/cancel.json")
    }
}
