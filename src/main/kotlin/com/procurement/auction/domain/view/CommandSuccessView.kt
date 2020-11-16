package com.procurement.auction.domain.view

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.infrastructure.web.response.version.ApiVersion

@JsonPropertyOrder("version", "id", "data")
class CommandSuccessView<T : View>(
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion,
    @field:JsonProperty("id") @param:JsonProperty("id") val id: CommandId,
    @field:JsonProperty("data") @param:JsonProperty("data") val data: T
) : View