package apavliuk.currencyrateservice.service.impl

import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import apavliuk.currencyrateservice.service.CurrenciesService
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
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
    companion object {
        private val logger = LoggerFactory.getLogger(CurrenciesServiceImpl::class.java)
    }

    override fun requestCurrencies(): Mono<CurrenciesRateResponse> {
        val fiatPath = "/fiat-currency-rates"
        val cryptoPath = "/crypto-currency-rates"

        val fiatCurrencyRate = getCurencyRates(fiatPath)
//            .onErrorResume {  }
//            .flatMap { c ->
//                currencyRepository.save(Currency(name = c.currency))
//            }
//        fiatCryptoCurrency.error

        val cryptoCurrencyRates = getCurencyRates(cryptoPath)
//        val result = Flux.zip(fiatCryptoCurrency, cryptoCurencyRates,
//            {f, c -> CurrencieRateResponse(f, c)}
//        )
        val result =
            Mono.zip(fiatCurrencyRate, cryptoCurrencyRates) { f, c ->
                logger.info("fiat is ${f}, crypto is $c")
                CurrenciesRateResponse(f, c)
            }

        return result
    }

    fun getCurencyRates(path: String): Mono<List<CurrenciesWebServiceResponse>> =
        webClient.post()
            .uri { uriBuilder -> uriBuilder.path(path).build() }
            .retrieve()
//            .onStatus(
//                { status ->  status.is5xxServerError },
//                { error ->  Mono.error(RuntimeException()) }
//            )
            .bodyToMono(object : ParameterizedTypeReference<List<CurrenciesWebServiceResponse>>() {})

}

class CurrenciesRateResponse(
    val fiat: List<CurrenciesWebServiceResponse>,
    val crypto: List<CurrenciesWebServiceResponse>
)

class CurrenciesWebServiceResponse(
    @JsonAlias("currency", "name")
    val currency: String,
    @JsonAlias("rate", "value")
    val rate: BigDecimal
)
