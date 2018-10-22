package com.procurement.auction.service

import com.procurement.auction.domain.Sign
import java.util.*

interface SignService {
    fun sign(): Sign
}

class SignServiceImpl : SignService {
    override fun sign(): Sign = UUID.randomUUID().toString()
}