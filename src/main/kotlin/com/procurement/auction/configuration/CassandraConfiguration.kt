package com.procurement.auction.configuration

import com.datastax.driver.core.Session
import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.cassandra.CassandraClusterBuilder
import com.procurement.auction.configuration.properties.CassandraProperties
import com.procurement.auction.repository.CalendarRepository
import com.procurement.auction.repository.CalendarRepositoryImpl
import com.procurement.auction.repository.EndedAuctionsRepository
import com.procurement.auction.repository.EndedAuctionsRepositoryImpl
import com.procurement.auction.repository.ScheduledAuctionsRepository
import com.procurement.auction.repository.ScheduledAuctionsRepositoryImpl
import com.procurement.auction.repository.SlotsRepository
import com.procurement.auction.repository.SlotsRepositoryImpl
import com.procurement.auction.repository.StartedAuctionsRepository
import com.procurement.auction.repository.StartedAuctionsRepositoryImpl
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    value = [
        CassandraProperties::class
    ]
)
@ComponentScan(basePackages = ["com.procurement.auction.repository"])
class CassandraConfiguration(
    private val objectMapper: ObjectMapper,
    private val cassandraProperties: CassandraProperties
) {

    @Bean
    fun session(): Session {
        val cluster = CassandraClusterBuilder.build(cassandraProperties)

        return if (cassandraProperties.keyspaceName != null)
            cluster.connect(cassandraProperties.keyspaceName)
        else
            cluster.connect()
    }

    @Bean
    fun calendarRepository(): CalendarRepository =
        CalendarRepositoryImpl(session())

    @Bean
    fun scheduledAuctionsRepository(): ScheduledAuctionsRepository =
        ScheduledAuctionsRepositoryImpl(objectMapper, session())

    @Bean
    fun slotsRepository(): SlotsRepository =
        SlotsRepositoryImpl(session())

    @Bean
    fun startedAuctionsRepository(): StartedAuctionsRepository =
        StartedAuctionsRepositoryImpl(objectMapper, session())

    @Bean
    fun endedAuctionsRepository(): EndedAuctionsRepository =
        EndedAuctionsRepositoryImpl(objectMapper, session())
}