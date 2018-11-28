package com.procurement.auction.domain.model

import com.fasterxml.jackson.databind.JsonSerializer

abstract class ValueObjectSerializer<T : ValueObject> : JsonSerializer<T>()