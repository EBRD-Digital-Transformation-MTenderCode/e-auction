package com.procurement.auction.domain.model.bid

import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.platformId.PlatformId
import com.procurement.auction.domain.model.sign.Sign
import com.procurement.auction.domain.model.value.Value
import java.time.LocalDateTime

class Bid(
    val id: BidId,
    val owner: PlatformId,
    val relatedLot: LotId,
    val value: Value,
    val pendingDate: LocalDateTime,
    val url: String,
    val sign: Sign
)