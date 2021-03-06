package com.procurement.auction.domain.view

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.infrastructure.web.response.version.ApiVersion

@JsonPropertyOrder("version", "id", "errors")
class CommandErrorView(
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion,
    @field:JsonProperty("id") @param:JsonProperty("id") val id: CommandId,
    @field:JsonProperty("errors") @param:JsonProperty("errors") val errors: List<Error>
) : View {
    @JsonPropertyOrder("code", "description")
    data class Error(
        @field:JsonProperty("code") @param:JsonProperty("code") private val code: String,
        @field:JsonProperty("description") @param:JsonProperty("description") private val description: String
    )
}