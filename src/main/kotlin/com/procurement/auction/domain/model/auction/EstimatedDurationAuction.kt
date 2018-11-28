package com.procurement.auction.domain.model.auction

import com.procurement.auction.domain.model.lotId.LotId
import java.time.Duration

data class EstimatedDurationAuction(
    val lotId: LotId,
    val duration: Duration
)