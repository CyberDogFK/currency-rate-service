package apavliuk.currencyrateservice.configuration

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer


@Configuration
class TestDataBaseConnection {
    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
//        val populator: CompositeDatabasePopulator = CompositeDatabasePopulator()
//        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))
//        initializer.setDatabasePopulator(populator)
        return initializer
    }
}