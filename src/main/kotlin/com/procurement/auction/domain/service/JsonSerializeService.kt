package com.procurement.auction.domain.service

interface JsonSerializeService {
    fun <T> serialize(obj: T): String
}