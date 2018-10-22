package com.procurement.auction.configuration

import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import com.procurement.auction.converter.AuctionConversionService
import com.procurement.auction.converter.AuctionConversionServiceImpl
import com.procurement.auction.converter.AuctionsDefinitionConverter
import com.procurement.auction.converter.AuctionsDefinitionConverterImpl
import com.procurement.auction.converter.EndedAuctionsConverter
import com.procurement.auction.converter.EndedAuctionsConverterImpl
import com.procurement.auction.converter.StartedAuctionsConverter
import com.procurement.auction.converter.StartedAuctionsConverterImpl
import com.procurement.auction.converter.addConverter
import com.procurement.auction.domain.request.auction.EndRQ
import com.procurement.auction.domain.request.auction.StartRQ
import com.procurement.auction.domain.request.schedule.ScheduleRQ
import com.procurement.auction.domain.schedule.AuctionsDefinition
import com.procurement.auction.entity.auction.EndedAuctions
import com.procurement.auction.entity.auction.StartedAuctions
import com.procurement.auction.service.SignService
import com.procurement.auction.service.SignServiceImpl
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    value = [
        AuctionProperties::class,
        SchedulerProperties::class
    ]
)
@ComponentScan(basePackages = ["com.procurement.auction.converter", "com.procurement.auction.service"])
class ServiceConfiguration(
    private val auctionProperties: AuctionProperties,
    private val cassandraConfiguration: CassandraConfiguration
) {
    @Bean
    fun auctionConversionService(): AuctionConversionService = AuctionConversionServiceImpl().also {
        it.addConverter<ScheduleRQ, AuctionsDefinition>(uctionsDefinitionConverter())
        it.addConverter<StartRQ, StartedAuctions?>(startedAuctionConverter())
        it.addConverter<EndRQ, EndedAuctions>(endedAuctionConverter())
    }

    @Bean
    fun signService(): SignService = SignServiceImpl()

    @Bean
    fun uctionsDefinitionConverter(): AuctionsDefinitionConverter = AuctionsDefinitionConverterImpl(auctionProperties)

    @Bean
    fun startedAuctionConverter(): StartedAuctionsConverter =
        StartedAuctionsConverterImpl(cassandraConfiguration.scheduledAuctionsRepository(), signService())

    @Bean
    fun endedAuctionConverter(): EndedAuctionsConverter =
        EndedAuctionsConverterImpl(cassandraConfiguration.startedAuctionsRepository())
}
