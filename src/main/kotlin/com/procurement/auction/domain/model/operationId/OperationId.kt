package com.procurement.auction.domain.model.operationId

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.auction.domain.extension.UUID_PATTERN
import com.procurement.auction.domain.extension.isUUID
import com.procurement.auction.domain.model.ValueObject

class OperationId private constructor(@JsonValue val underlying: String) : ValueObject {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is OperationId
                && this.underlying == other.underlying
        else
            true
    }

    override fun hashCode(): Int = underlying.hashCode()

    override fun toString(): String = underlying

    companion object {
        val pattern: String
            get() = UUID_PATTERN

        fun validation(text: String): Boolean = text.isUUID()

        fun tryCreateOrNull(text: String): OperationId? = if (validation(text)) OperationId(text) else null
    }
}
