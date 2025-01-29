package apavliuk.currencyrateservice.repository.impl

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.repository.CurrencyRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class CurrencyRepositoryImpl(
    private val databaseClient: DatabaseClient
): CurrencyRepository {
    override fun findCurrencyByName(name: String): Mono<Currency> =
        databaseClient.sql("SELECT * FROM currency " +
            "inner join currency_type on currency.type_id = currency_type.id " +
            "where currency.name=:name limit 1")
        .bind("name", name)
        .map { row, metadata ->
            Currency(row.get("currency.id", Long::class.java), row.get("currency.name", String::class.java)!!,
                CurrencyType(row.get("currency_type.id", Long::class.java), row.get("currency_type.name", String::class.java)!!))
        }
        .one()

    override fun save(currency: Currency): Mono<Currency> =
        databaseClient.sql("INSERT INTO currency (name, type_id) VALUES (:name, :type_id)")
            .bind("name", currency.name)
            .bind("type_id", currency.type.id!!)
            .fetch()
            .one()
            .map {
                Currency(it["id"] as Long, currency.name, currency.type)
            }
}