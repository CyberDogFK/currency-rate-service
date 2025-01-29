package apavliuk.currencyrateservice.repository.impl

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.HistoricalRate
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class HistoricalRateRepositoryImpl(
    private val databaseClient: DatabaseClient
): HistoricalRateRepository {
    override fun save(historicalRate: HistoricalRate): Mono<HistoricalRate> =
        databaseClient.sql("INSERT INTO historical_rate (currency_id, timestamp, rate) VALUES (:currency_id, :timestamp, :rate)")
            .bind("currency_id", historicalRate.currency.id!!)
            .bind("timestamp", historicalRate.timestamp)
            .bind("rate", historicalRate.rate)
            .fetch()
            .one()
            .map {
                HistoricalRate(it["id"] as Long, historicalRate.currency, historicalRate.timestamp, historicalRate.rate)
            }

//    override fun save(currency: Currency): Mono<Currency> =
//        databaseClient.sql("INSERT INTO currency (name, type_id) VALUES (:name, :type_id)")
//            .bind("name", currency.name)
//            .bind("type_id", currency.type.id!!)
//            .fetch()
//            .one()
//            .map {
//                Currency(it["id"] as Long, currency.name, currency.type)
//            }
}