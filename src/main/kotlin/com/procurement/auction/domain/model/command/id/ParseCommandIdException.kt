package com.procurement.auction.domain.model.command.id

class ParseCommandIdException(commandId: String) : RuntimeException("Error of parsing command id '$commandId'.")