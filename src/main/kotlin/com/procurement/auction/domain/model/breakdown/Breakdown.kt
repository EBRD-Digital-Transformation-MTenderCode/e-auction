package com.procurement.auction.domain.model.breakdown

import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.breakdown.status.BreakdownStatus
import com.procurement.auction.domain.model.value.Value
import java.time.LocalDateTime

class Breakdown(
    val relatedBid: BidId,
    val status: BreakdownStatus,
    val dateMet: LocalDateTime,
    val value: Value
)