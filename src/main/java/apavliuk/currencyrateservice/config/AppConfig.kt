package apavliuk.currencyrateservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class AppConfig {
    @Value("\${currencies-service.url}")
    private lateinit var serviceUrl: String

    @Value("\${currencies-service.api-key}")
    private var apiKey: String? = null

    @Bean
    fun getWebClient(): WebClient =
        WebClient.builder()
            .baseUrl(serviceUrl)
            .defaultHeader("X-API-KEY", apiKey)
            .build()
}