package com.procurement.auction.infrastructure.cassandra

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.ProtocolVersion
import com.datastax.driver.core.QueryOptions
import com.datastax.driver.core.SocketOptions
import com.procurement.auction.configuration.properties.CassandraProperties
import org.springframework.beans.BeanUtils
import org.springframework.util.StringUtils

object CassandraClusterBuilder {
    fun build(cassandraProperties: CassandraProperties): Cluster {
        val builder = Cluster.builder()

        cassandraProperties.clusterName?.also {
            builder.withClusterName(cassandraProperties.clusterName)
        }

        builder.withPort(cassandraProperties.port)

        cassandraProperties.username?.also {
            builder.withCredentials(cassandraProperties.username, cassandraProperties.password)
        }

        builder.withCompression(cassandraProperties.compression)

        cassandraProperties.loadBalancingPolicy?.also {
            val policy = instantiate(it)
            builder.withLoadBalancingPolicy(policy)
        }

        builder.withQueryOptions(getQueryOptions(cassandraProperties))

        cassandraProperties.reconnectionPolicy?.also {
            val policy = instantiate(it)
            builder.withReconnectionPolicy(policy)
        }

        cassandraProperties.retryPolicy?.also {
            val policy = instantiate(it)
            builder.withRetryPolicy(policy)
        }

        builder.withSocketOptions(getSocketOptions(cassandraProperties))

        if (cassandraProperties.isSsl) {
            builder.withSSL()
        }

        builder.withPoolingOptions(getPoolingOptions(cassandraProperties))

        val points = cassandraProperties.contactPoints
        builder.addContactPoints(*StringUtils.commaDelimitedListToStringArray(points))

        builder.withProtocolVersion(ProtocolVersion.NEWEST_SUPPORTED)

        return builder.withoutJMXReporting().build()
    }

    private fun <T> instantiate(type: Class<out T>): T {
        return BeanUtils.instantiateClass(type)
    }

    private fun getQueryOptions(cassandraProperties: CassandraProperties): QueryOptions = QueryOptions()
        .also { options ->
            cassandraProperties.consistencyLevel?.also { options.consistencyLevel = it }
            cassandraProperties.serialConsistencyLevel?.also { options.serialConsistencyLevel = it }
            options.fetchSize = cassandraProperties.fetchSize
        }

    private fun getSocketOptions(cassandraProperties: CassandraProperties): SocketOptions = SocketOptions()
        .also { options ->
            cassandraProperties.connectTimeout?.also { options.connectTimeoutMillis = it.toMillis().toInt() }
            cassandraProperties.readTimeout?.also { options.readTimeoutMillis = it.toMillis().toInt() }
        }

    private fun getPoolingOptions(cassandraProperties: CassandraProperties): PoolingOptions = PoolingOptions()
        .also { options ->
            val pool = cassandraProperties.pool
            options.idleTimeoutSeconds = pool.idleTimeout.seconds.toInt()
            options.poolTimeoutMillis = pool.poolTimeout.toMillis().toInt()
            options.heartbeatIntervalSeconds = pool.heartbeatInterval.seconds.toInt()
            options.maxQueueSize = pool.maxQueueSize
        }
}