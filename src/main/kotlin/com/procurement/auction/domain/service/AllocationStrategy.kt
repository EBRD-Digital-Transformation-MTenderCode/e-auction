package com.procurement.auction.domain.service

import com.procurement.auction.domain.model.auction.EstimatedDurationAuction
import com.procurement.auction.domain.model.bucket.AuctionsTimes
import com.procurement.auction.domain.model.slots.Slot
import java.time.LocalDate

interface AllocationStrategy {
    fun allocation(startDate: LocalDate,
                   estimates: List<EstimatedDurationAuction>,
                   slots: Collection<Slot>): AuctionsTimes?
}