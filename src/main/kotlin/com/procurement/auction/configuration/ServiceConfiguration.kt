package com.procurement.auction.configuration

import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    value = [
        AuctionProperties::class,
        SchedulerProperties::class
    ]
)
@ComponentScan(
    basePackages = [
        "com.procurement.auction.domain.factory",
        "com.procurement.auction.domain.service",
        "com.procurement.auction.application.service",
        "com.procurement.auction.application.presenter",
        "com.procurement.auction.infrastructure.dispatcher"
    ]
)
class ServiceConfiguration
