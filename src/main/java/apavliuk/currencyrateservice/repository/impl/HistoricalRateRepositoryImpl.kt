package apavliuk.currencyrateservice.repository.impl

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.HistoricalRate
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class HistoricalRateRepositoryImpl(
    private val databaseClient: DatabaseClient
): HistoricalRateRepository {
    companion object {
        private val logger = LoggerFactory.getLogger(HistoricalRateRepositoryImpl::class.java)
    }

    override fun save(historicalRate: HistoricalRate): Mono<HistoricalRate> =
        databaseClient.sql("INSERT INTO historical_rate (currency_id, timestamp, rate) VALUES (:currency_id, :timestamp, :rate) RETURNING id")
            .bind("currency_id", historicalRate.currency.id!!)
            .bind("timestamp", historicalRate.timestamp)
            .bind("rate", historicalRate.rate)
            .flatMap { it ->
                it.map { row, metadata ->
                    val id = row.get("id", Long::class.java)
                    HistoricalRate(id, historicalRate.currency, historicalRate.timestamp, historicalRate.rate)
                }
            }.next()
}
