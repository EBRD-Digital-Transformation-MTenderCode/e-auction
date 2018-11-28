package com.procurement.auction.domain.model.auction.id

import com.procurement.auction.domain.model.ValueObject
import java.util.*

data class AuctionId(val value: UUID = UUID.randomUUID()) : ValueObject