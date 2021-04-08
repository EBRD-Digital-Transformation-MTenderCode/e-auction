package com.procurement.auction.infrastructure.dto.command.auction.validate

import com.procurement.auction.infrastructure.dto.AbstractDTOTestBase
import com.procurement.auction.infrastructure.service.handler.auction.validate.ValidateAuctionsDataRequest
import org.junit.jupiter.api.Test

class ValidateAuctionsDataRequestTest : AbstractDTOTestBase<ValidateAuctionsDataRequest>(ValidateAuctionsDataRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/command/auction/validate/validate_auctions_data.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/command/auction/validate/validate_auctions_data_required_1.json")
    }
}
