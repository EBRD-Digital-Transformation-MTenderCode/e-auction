package com.procurement.auction.infrastructure.service.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.auction.infrastructure.service.command.type.Action

interface Handler<T : Action, R : Any> {
    val action: T
    fun handle(node: JsonNode): R
}