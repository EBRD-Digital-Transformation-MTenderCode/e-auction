package com.procurement.auction.domain.model.version

import com.procurement.auction.domain.model.ValueObject

data class RowVersion(val original: Int, val modified: Int) : ValueObject {
    companion object {
        private const val VERSION_OF_NEW_RECORD = 0

        fun of(): RowVersion = RowVersion(VERSION_OF_NEW_RECORD, VERSION_OF_NEW_RECORD)

        fun of(value: Int): RowVersion = RowVersion(value, value)

        @JvmStatic
        private fun next(version: RowVersion): RowVersion {
            val original = version.original
            val modified = version.modified + 1

            if (version.original != version.modified)
                throw IllegalStateException("Version was been changed more than one time (original: '$original', modified: '$modified').")

            return RowVersion(original, modified)
        }
    }

    fun next(): RowVersion = RowVersion.next(this)

    val hasChanged: Boolean
        get() = original != modified

    val isNew: Boolean
        get() = original == VERSION_OF_NEW_RECORD
}