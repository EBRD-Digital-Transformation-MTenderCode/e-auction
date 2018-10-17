package com.procurement.auction

import com.procurement.auction.configuration.ApplicationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackageClasses = [
        ApplicationConfiguration::class
    ]
)
class AuctionApplication

fun main(args: Array<String>) {
    runApplication<AuctionApplication>(*args)
}
