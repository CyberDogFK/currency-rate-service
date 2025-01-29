package apavliuk.currencyrateservice.service.impl

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse
import apavliuk.currencyrateservice.dto.CurrenciesWebServiceResponse
import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.model.HistoricalRate
import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import apavliuk.currencyrateservice.service.CurrenciesService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.stream.Collectors

@Service
class CurrenciesServiceImpl(
    private val webClient: WebClient,
    private val currencyRepository: CurrencyRepository,
    private val historicalRateRepository: HistoricalRateRepository,
): CurrenciesService {
    enum class PreparedCurrencyType(val type: CurrencyType, val urlPath: String) {
        Fiat(CurrencyType(1, "fiat"), "/fiat-currency-rates"),
        Crypto(CurrencyType(2, "crypto"), "/crypto-currency-rates")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CurrenciesServiceImpl::class.java)
    }

    override fun requestCurrencies(): Mono<CurrenciesRateResponse> {
        val fiatCurrency = PreparedCurrencyType.Fiat
        val cryptoCurrency = PreparedCurrencyType.Crypto

        val unixTimestamp = LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .toEpochSecond()


        val fiatCurrencyRate = requestCurrencyRates(fiatCurrency.urlPath)
            .flatMap { saveCurrencyFromResponse(it, fiatCurrency.type
                , unixTimestamp) }
            .switchIfEmpty(getFinalRateForCurrencyType(fiatCurrency.type))
            .collect(Collectors.toList())

        val cryptoCurrencyRates = requestCurrencyRates(cryptoCurrency.urlPath)
            .flatMap { saveCurrencyFromResponse(it, cryptoCurrency.type, unixTimestamp)}
            .switchIfEmpty(getFinalRateForCurrencyType(cryptoCurrency.type))
            .collect(Collectors.toList())

        return Mono.zip(fiatCurrencyRate, cryptoCurrencyRates) { f, c ->
            CurrenciesRateResponse(
                fiat = f.map { CurrenciesWebServiceResponse(it.currency.name, it.rate) },
                crypto = c.map { CurrenciesWebServiceResponse(it.currency.name, it.rate) },
                )
            }
    }

    private fun getFinalRateForCurrencyType(currencyType: CurrencyType): Flux<HistoricalRate> =
        Flux.defer {
            val currencies = currencyRepository.findCurrencyByType(currencyType)
            currencies.flatMap { c ->
                historicalRateRepository.finaLastRateForCurrency(c)
            }
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
