package com.procurement.auction.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    value = [
        WebConfiguration::class,
        ServiceConfiguration::class,
        CassandraConfiguration::class,
        ActuatorSecurity::class
    ]
)
class ApplicationConfiguration
