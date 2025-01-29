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
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
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

        val fiatCurrencyRate = requestCurrencyRates(fiatPath)
            .flatMap { saveCurrencyFromResponse(it, fiatType, unixTimestamp) }
            .switchIfEmpty(Flux.defer {
                val currencies = currencyRepository.findCurrencyByType(fiatType)
                currencies.flatMap { c ->
                    historicalRateRepository.finaLastRateForCurrency(c)
                }
            })
            .collect(Collectors.toList())

        val cryptoCurrencyRates = requestCurrencyRates(cryptoPath)
            .flatMap { saveCurrencyFromResponse(it, cryptoType, unixTimestamp)}
            .switchIfEmpty(Flux.defer {
                val currencies = currencyRepository.findCurrencyByType(cryptoType)
                currencies.flatMap { c ->
                    historicalRateRepository.finaLastRateForCurrency(c)
                }
            })
            .collect(Collectors.toList())

        val result = Mono.zip(fiatCurrencyRate, cryptoCurrencyRates) { f, c ->
            CurrenciesRateResponse(
                fiat = f.map { CurrenciesWebServiceResponse(it.currency.name, it.rate) },
                crypto = c.map { CurrenciesWebServiceResponse(it.currency.name, it.rate) },
                )
            }

        return result
    }

    private fun requestCurrencyRates(path: String): Flux<CurrenciesWebServiceResponse> =
        webClient.post()
            .uri { uriBuilder -> uriBuilder.path(path).build() }
            .retrieve()
            .bodyToFlux(CurrenciesWebServiceResponse::class.java)
            .onErrorResume {
                logger.warn("Error, fallback to empty list", it)
                Mono.empty()
            }

    private fun saveCurrencyFromResponse(response: CurrenciesWebServiceResponse, type: CurrencyType, unixTimestamp: Long): Mono<HistoricalRate> =
        currencyRepository.findCurrencyByName(response.currency)
            .switchIfEmpty(
                // saving new currency
                currencyRepository.save(Currency(name = response.currency, type = type))
            )
            .flatMap {
                logger.info("saving historical rate")
                historicalRateRepository.save(HistoricalRate(currency = it,
                    timestamp = unixTimestamp,
                    rate = response.rate))
            }

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
