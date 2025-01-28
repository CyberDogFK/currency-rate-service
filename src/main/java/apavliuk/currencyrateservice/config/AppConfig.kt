package apavliuk.currencyrateservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
}