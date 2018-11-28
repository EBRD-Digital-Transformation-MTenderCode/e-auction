package com.procurement.auction.exception.json

class JsonParseToObjectException(json: String, exception: Throwable)
    : RuntimeException("Error of parsing JSON.\n${exception.message}", exception)