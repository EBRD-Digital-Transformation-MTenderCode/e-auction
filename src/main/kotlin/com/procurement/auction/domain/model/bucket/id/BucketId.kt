package com.procurement.auction.domain.model.bucket.id

import com.procurement.auction.domain.model.ValueObject
import com.procurement.auction.domain.model.country.Country
import java.time.LocalDate

data class BucketId(val date: LocalDate, val country: Country) : ValueObject