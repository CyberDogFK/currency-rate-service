package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.HistoricalRate
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface HistoricalRateRepository: ReactiveCrudRepository<HistoricalRate, Long> {
}