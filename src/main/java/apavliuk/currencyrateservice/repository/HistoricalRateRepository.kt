package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.model.HistoricalRate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface HistoricalRateRepository {
    fun save(historicalRate: HistoricalRate): Mono<HistoricalRate>

    fun finaLastRateForCurrency(currency: Currency): Mono<HistoricalRate>
}