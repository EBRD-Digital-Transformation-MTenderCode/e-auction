package com.procurement.auction.domain.model.ocid

class ParseOcidException(ocid: String) : RuntimeException("Error of parsing ocid '$ocid'")