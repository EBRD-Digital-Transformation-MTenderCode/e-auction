package com.procurement.auction.exception

class JsonParseToObjectException(json: String, exception: Throwable)
    : RuntimeException("Error of parsing JSON:\n$json", exception)