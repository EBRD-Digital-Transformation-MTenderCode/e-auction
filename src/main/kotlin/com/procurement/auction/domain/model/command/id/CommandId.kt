package com.procurement.auction.domain.model.command.id

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.auction.domain.extension.UUID_PATTERN
import com.procurement.auction.domain.extension.isUUID
import com.procurement.auction.domain.model.ValueObject
import java.util.*

class CommandId private constructor(@JsonValue val underlying: String) : ValueObject {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is CommandId
                && this.underlying == other.underlying
        else
            true
    }

    override fun hashCode(): Int = underlying.hashCode()

    override fun toString(): String = underlying

    companion object {
        val NaN = CommandId(UUID(0, 0).toString())

        val pattern: String
            get() = UUID_PATTERN

        fun validation(text: String): Boolean = text.isUUID()

        fun tryCreateOrNull(text: String): CommandId? = if (validation(text)) CommandId(text) else null
    }
}
