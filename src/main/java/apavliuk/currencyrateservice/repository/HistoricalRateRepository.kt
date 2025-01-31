package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.HistoricalRate
import reactor.core.publisher.Mono

interface HistoricalRateRepository {
    fun save(historicalRate: HistoricalRate): Mono<HistoricalRate>

    fun findLastRateForCurrency(currency: Currency): Mono<HistoricalRate>

    fun deleteAll(): Mono<Void>
}
