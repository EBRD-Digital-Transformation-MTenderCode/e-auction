package com.procurement.auction.domain.model

import com.fasterxml.jackson.databind.JsonDeserializer

abstract class ValueObjectDeserializer<T : ValueObject> : JsonDeserializer<T>()