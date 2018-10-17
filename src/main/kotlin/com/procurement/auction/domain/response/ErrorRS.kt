package com.procurement.auction.domain.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

data class ErrorRS @JsonCreator constructor(@field:JsonProperty("errors")
                                            @param:JsonProperty("errors") val errors: List<Error>) {

    @JsonPropertyOrder("code", "description")
    data class Error @JsonCreator
    constructor(@field:JsonProperty("code")
                @param:JsonProperty("code")
                private val code: String,

                @field:JsonProperty("description")
                @param:JsonProperty("description")
                private val description: String
    )
}

