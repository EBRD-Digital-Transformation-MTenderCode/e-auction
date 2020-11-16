package com.procurement.auction.domain.model.operationId

class ParseOperationIdException(operationId: String) : RuntimeException("Error of parsing operationId '$operationId'")