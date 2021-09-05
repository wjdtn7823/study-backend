package com.boo.study.config

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.h2.tools.Server
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.SQLException
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(basePackages = ["com.boo.study.aggregate"])
//@EnableJpaAuditing(auditorAwareRef = "auditor")
@EnableConfigurationProperties(DataSourcePropertiesGroup::class)
class DataBaseConfig {

    @Bean
    fun dataSource(dataSourcePropertiesGroup: DataSourcePropertiesGroup): DataSource {
        val dataSourcePropertiesPair: DataSourcePropertiesPair = dataSourcePropertiesGroup.getDataSourceProperties()

        require(dataSourcePropertiesPair.first.dataSourceType == DataSourcePropertiesGroup.DataSourceType.MASTER)
        require(dataSourcePropertiesPair.second.dataSourceType == DataSourcePropertiesGroup.DataSourceType.SLAVE)

        val masterDataSource = HikariDataSource(dataSourcePropertiesPair.first.dataSourceProperties)
        val dataSourceMap = mapOf(
            dataSourcePropertiesPair.first.dataSourceType as Any to masterDataSource as Any,
            dataSourcePropertiesPair.second.dataSourceType as Any to HikariDataSource(dataSourcePropertiesPair.second.dataSourceProperties) as Any)

        val replicationRoutingDataSource: AbstractRoutingDataSource = object : AbstractRoutingDataSource() {
            override fun determineCurrentLookupKey(): Any? {
                return if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                    log.debug { "Current DataSource : SLAVE" }
                    DataSourcePropertiesGroup.DataSourceType.SLAVE
                } else {
                    log.debug { "Current DataSource : MASTER" }
                    DataSourcePropertiesGroup.DataSourceType.MASTER
                }
            }
        }
        replicationRoutingDataSource.setTargetDataSources(dataSourceMap)
        replicationRoutingDataSource.setDefaultTargetDataSource(masterDataSource)
        replicationRoutingDataSource.afterPropertiesSet()
        return LazyConnectionDataSourceProxy(replicationRoutingDataSource)
    }

    @Profile("local_h2")
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Throws(SQLException::class)
    fun h2TcpServer(): Server {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092")
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}