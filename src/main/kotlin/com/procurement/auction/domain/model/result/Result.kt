package com.procurement.auction.domain.model.result

import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.value.Value

class Result(
    val relatedBid: BidId,
    val value: Value
)