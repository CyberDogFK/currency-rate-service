package apavliuk.currencyrateservice.repository.impl

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.HistoricalRate
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
class HistoricalRateRepositoryImpl(
    private val databaseClient: DatabaseClient
): HistoricalRateRepository {

    override fun save(historicalRate: HistoricalRate): Mono<HistoricalRate> =
        databaseClient.sql("INSERT INTO historical_rate (currency_id, timestamp, rate) VALUES (:currency_id, :timestamp, :rate) RETURNING id")
            .bind("currency_id", historicalRate.currency.id!!)
            .bind("timestamp", historicalRate.timestamp)
            .bind("rate", historicalRate.rate)
            .map { row, metadata ->
                    val id = row.get("id", Long::class.java)
                    HistoricalRate(
                        id,
                        historicalRate.currency,
                        historicalRate.timestamp,
                        historicalRate.rate
                    )
            }.one()

    override fun finaLastRateForCurrency(currency: Currency): Mono<HistoricalRate> =
        databaseClient.sql("SELECT * from historical_rate where currency_id=:currency_id order by timestamp desc limit 1")
            .bind("currency_id", currency.id!!)
            .map { row, metadata ->
                HistoricalRate(
                    row.get("id", Long::class.java),
                    currency = currency,
                    row.get("timestamp", Long::class.java)!!,
                    row.get("rate", BigDecimal::class.java)!!,
                )
            }.one()
}
