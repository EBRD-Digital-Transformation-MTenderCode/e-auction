package com.procurement.auction.domain.model.sign

import com.procurement.auction.domain.model.ValueObject
import java.util.*

data class Sign(val value: UUID = UUID.randomUUID()) : ValueObject