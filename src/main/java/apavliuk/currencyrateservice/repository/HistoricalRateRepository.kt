package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.HistoricalRate
import reactor.core.publisher.Mono

interface HistoricalRateRepository {
    fun save(historicalRate: HistoricalRate): Mono<HistoricalRate>
}