package apavliuk.currencyrateservice.service.impl

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import apavliuk.currencyrateservice.service.CurrenciesService
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.URI

@Service
class CurrenciesServiceImpl(
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val currencyRepository: CurrencyRepository,
    private val historicalRateRepository: HistoricalRateRepository,
): CurrenciesService {
    override fun requestCurrencies(): Mono<String> {
        val fiat = URI.create("/fiat-currency-rates")
        val crypto = URI.create("/crypto-currency-rates")

        val fiatCryptoCurrency = getCurencyRates(fiat)
//            .onErrorResume {  }
//            .flatMap { c ->
//                currencyRepository.save(Currency(name = c.currency))
//            }
//        fiatCryptoCurrency.error

        val cryptoCurencyRates = getCurencyRates(crypto)
        Flux.zip(fiatCryptoCurrency, cryptoCurencyRates, )


        val some = listOf("")

        TODO()
    }

    val getCurencyRates: (uri: URI) -> Flux<CurrenciesWebServiceResponse> = {
        webClient.post()
            .uri("/fiat-currency-rates")
            .retrieve()
//            .onStatus(
//                { status ->  status.is5xxServerError },
//                { error ->  Mono.error(RuntimeException()) }
//            )
            .bodyToFlux(CurrenciesWebServiceResponse::class.java)
    }
}

class CurrencieRateResponse(
    val fiat: List<CurrenciesWebServiceResponse>,
    val rate: List<CurrenciesWebServiceResponse>
)

class CurrenciesWebServiceResponse(
    @JsonAlias("currency", "name")
    val currency: String,
    @JsonAlias("rate", "value")
    val rate: BigDecimal
)
