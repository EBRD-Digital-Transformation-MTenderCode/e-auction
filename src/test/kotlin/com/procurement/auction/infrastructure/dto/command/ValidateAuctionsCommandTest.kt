package com.procurement.auction.infrastructure.dto.command

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class ValidateAuctionsCommandTest : AbstractDTOTestBase<ValidateAuctionsCommand>(ValidateAuctionsCommand::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/command/validate.json")
    }
}
