package com.procurement.auction.domain.model.slots.id

import com.procurement.auction.domain.model.ValueObject
import java.util.*

data class SlotId(val value: UUID = UUID.randomUUID()) : ValueObject