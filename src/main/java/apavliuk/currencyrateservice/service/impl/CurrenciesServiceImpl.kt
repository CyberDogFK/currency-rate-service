package apavliuk.currencyrateservice.service.impl

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.model.HistoricalRate
import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.CurrencyTypeRepository
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.stream.Collectors

@Service
class CurrenciesServiceImpl(
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val currencyRepository: CurrencyRepository,
    private val historicalRateRepository: HistoricalRateRepository,
    private val currencyTypeRepository: CurrencyTypeRepository,
): CurrenciesService {
    // fiat id 1, crypto id 2

    companion object {
        private val logger = LoggerFactory.getLogger(CurrenciesServiceImpl::class.java)
    }

    override fun requestCurrencies(): Mono<CurrenciesRateResponse> {
        val fiatType = CurrencyType(1, name = "fiat")
        val cryptoType = CurrencyType(2, name = "crypto")
        val fiatPath = "/fiat-currency-rates"
        val cryptoPath = "/crypto-currency-rates"
        val unixTimestamp = LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()

        val fiatCurrencyRate = getCurencyRates(fiatPath)
            .flatMap { response ->
                val savedCurrency = currencyRepository.findCurrencyByName(response.currency)
                    .switchIfEmpty(
                        currencyRepository.save(Currency(name = response.currency, type = fiatType))
//                        Flux.just(Currency(1,"", cryptoType))
                    )
                    .map {
                        logger.info("saving historaical rate")
                        historicalRateRepository.save(HistoricalRate(currency = it,
                            timestamp = unixTimestamp,
                            rate = response.rate))
                    }
                savedCurrency
            }
            .flatMap { it }
            .collect(Collectors.toList())
//        val savingFiat = currencyRepository
//                    .saveAll(fiatCurrencyRate.map { Currency(name = it.currency, type = fiatType) })


        val cryptoCurrencyRates = Mono.just(listOf(HistoricalRate(0, Currency(1,"", cryptoType), 0L, BigDecimal.ONE)))
//        val cryptoCurrencyRates = getCurencyRates(cryptoPath)
//            .flatMap { response ->
//                val savedCurrency = currencyRepository.findCurrencyByName(response.currency)
//                    .switchIfEmpty(
//                        currencyRepository.save(Currency(name = response.currency, type = cryptoType))
//                    )
//                    .map {
//                        historicalRateRepository.save(HistoricalRate(currency = it,
//                            timestamp = unixTimestamp,
//                            rate = response.rate))
//                    }
//                savedCurrency
//            }
//            .flatMap { it }
//            .collect(Collectors.toList())

        val result = Mono.zip(fiatCurrencyRate, cryptoCurrencyRates) { f, c ->
                logger.info("fiat is ${f}, crypto is $c")
            CurrenciesRateResponse(
                fiat = f.map { CurrenciesWebServiceResponse(it.currency.name, it.rate) },
                crypto = c.map { CurrenciesWebServiceResponse(it.currency.name, it.rate) },
                )
            }
//        Mono.zip(fiatCurrencyRate, cryptoCurrencyRates) {
//
//        }

        return result
    }

    fun getCurencyRates(path: String): Flux<CurrenciesWebServiceResponse> =
        webClient.post()
            .uri { uriBuilder -> uriBuilder.path(path).build() }
            .retrieve()
//            .onStatus(
//                { status ->  status.is5xxServerError },
//                { error ->  Mono.error(RuntimeException()) }
//            )
            .bodyToFlux(CurrenciesWebServiceResponse::class.java)

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
