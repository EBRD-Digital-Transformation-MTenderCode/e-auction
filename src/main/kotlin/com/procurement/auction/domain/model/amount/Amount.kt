package com.procurement.auction.domain.model.amount

import com.procurement.auction.domain.model.ValueObject
import java.math.BigDecimal

data class Amount(val value: BigDecimal) : ValueObject {
    init {
        if (value <= BigDecimal.ZERO)
            throw IllegalArgumentException("The value less then zero.")
    }
}