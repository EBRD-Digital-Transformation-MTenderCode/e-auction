package com.procurement.auction.configuration.properties

import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import java.util.*

object GlobalProperties2 {
    val service = Service()

    object App {
        val apiVersion = ApiVersion(major = 1, minor = 0, patch = 0)
    }

    class Service {
        val id: String = "15"
        val name: String = "e-auction"
        val version: String = loadVersion()

        private fun loadVersion(): String {
            val gitProps: Properties = try {
                GlobalProperties2::class.java.getResourceAsStream("/git.properties")
                    .use { stream ->
                        Properties().apply { load(stream) }
                    }
            } catch (expected: Exception) {
                throw IllegalStateException(expected)
            }
            return gitProps.orThrow("git.commit.id.abbrev")
        }
    }
}

fun Properties.orThrow(name: String): String = this[name]
    ?.toString()
    ?: throw IllegalStateException("Property '$name' is not found.")

