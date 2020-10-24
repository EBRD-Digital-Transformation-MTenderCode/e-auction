package com.procurement.auction.configuration

import com.procurement.auction.application.service.Logger
import com.procurement.auction.infrastructure.service.logger.CustomLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoggerConfiguration {

    @Bean
    fun logger(): Logger = CustomLogger()
}