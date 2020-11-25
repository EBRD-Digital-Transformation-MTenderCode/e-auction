package com.procurement.auction.domain.model.cpid

class ParseCpidException(cpid: String) : RuntimeException("Error of parsing cpid '$cpid'")