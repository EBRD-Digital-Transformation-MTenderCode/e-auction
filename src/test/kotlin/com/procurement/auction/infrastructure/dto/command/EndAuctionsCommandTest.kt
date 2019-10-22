package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class EndAuctionsCommandTest : AbstractDTOTestBase<EndAuctionsCommand>(EndAuctionsCommand::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/command/end.json")
    }
}
