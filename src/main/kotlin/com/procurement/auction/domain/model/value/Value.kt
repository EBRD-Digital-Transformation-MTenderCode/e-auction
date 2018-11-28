package com.procurement.auction.domain.model.value

import com.procurement.auction.domain.model.ValueObject
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.currency.Currency

data class Value(val amount: Amount, val currency: Currency) : ValueObject