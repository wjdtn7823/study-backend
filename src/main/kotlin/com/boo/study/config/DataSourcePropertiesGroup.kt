package com.boo.study.config

import com.zaxxer.hikari.HikariConfig
import mu.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties(prefix = "database")
@PropertySource("classpath:/database/db-\${spring.profiles.active}.properties")
class DataSourcePropertiesGroup {
    // NOTE: private 필드로 선언하면 yml 파일에서 설정할 수 없음
    var dataSource: DataSourceProperties = DataSourceProperties()
    var dataSources: MutableMap<DataSourceType, DataSourceProperties> = mutableMapOf()

    fun getDataSourceProperties(): DataSourcePropertiesPair {
        require(dataSources.size == 2 || dataSource.valid()) { "DataSource configuration is not defined." }
        return if (dataSource.valid()) {
            if (dataSources.isNotEmpty()) {
                val keys = dataSources.keys.joinToString { it.toString() }
                log.warn { "Ignored DataSource configuration: $keys" }
            }
            Pair(DataSourcePropertiesWrapper(DataSourceType.MASTER, dataSource),
                DataSourcePropertiesWrapper(DataSourceType.SLAVE, dataSource))
        } else {
            val master = dataSources[DataSourceType.MASTER]!!.check()
            val slave = dataSources[DataSourceType.SLAVE]!!.check()
            Pair(DataSourcePropertiesWrapper(DataSourceType.MASTER, master),
                DataSourcePropertiesWrapper(DataSourceType.SLAVE, slave))
        }
    }

    class DataSourceProperties : HikariConfig() {
        var platform: Platform? = null
        var initializationMode: InitializationMode? = null

        fun check(): DataSourceProperties {
            require(!driverClassName.isNullOrBlank()) { "Driver Class Name is not defined." }
            require(!jdbcUrl.isNullOrBlank()) { "Url is not defined." }
            require(!username.isNullOrBlank()) { "User Name is not defined." }
            return this
        }

        fun valid(): Boolean = (!driverClassName.isNullOrBlank()) && (!jdbcUrl.isNullOrBlank()) && (!username.isNullOrBlank())
    }

    data class DataSourcePropertiesWrapper(
        val dataSourceType: DataSourceType,
        val dataSourceProperties: DataSourceProperties
    )

    enum class DataSourceType { MASTER, SLAVE }
    enum class Platform { MYSQL, H2 }
    enum class InitializationMode { ALWAYS, NEVER }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}

typealias DataSourcePropertiesPair
        = Pair<DataSourcePropertiesGroup.DataSourcePropertiesWrapper, DataSourcePropertiesGroup.DataSourcePropertiesWrapper>



