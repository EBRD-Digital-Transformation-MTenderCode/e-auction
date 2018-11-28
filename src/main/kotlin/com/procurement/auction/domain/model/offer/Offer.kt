package com.procurement.auction.domain.model.offer

import com.procurement.auction.domain.model.breakdown.Breakdown
import com.procurement.auction.domain.model.period.Period
import com.procurement.auction.domain.model.progressId.ProgressId

class Offer(
    val id: ProgressId,
    val period: Period,
    val breakdowns: List<Breakdown>
)