package apavliuk.currencyrateservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.joselion.springr2dbcrelationships.R2dbcRelationshipsCallbacks
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class AppConfig {
    @get:Bean
    val webClient = WebClient.builder() // move to bean
        .baseUrl("http://localhost:8085")
        .defaultHeader("X-API-KEY", "secret-key")
        .build()

    @get:Bean
    val objectMapper = ObjectMapper()

//    @Bean
//    fun <T> relationshipsCallbacks(
//        @Lazy template: R2dbcEntityTemplate?,
//        context: ApplicationContext
//    ): R2dbcRelationshipsCallbacks<T?> {
//        return R2dbcRelationshipsCallbacks(template, context)
//    }
}