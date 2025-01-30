package apavliuk.currencyrateservice.repository.impl

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.repository.CurrencyRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class CurrencyRepositoryImpl(
    private val databaseClient: DatabaseClient
): CurrencyRepository {
    override fun findCurrencyByName(name: String): Mono<Currency> =
        databaseClient.sql(
            "SELECT c.id c_id, c.name c_name, c.type_id c_type_id, ct.id ct_id, ct.name ct_name FROM currency c " +
            "inner join currency_type ct on c.type_id = ct.id " +
            "where c.name=:name limit 1")
        .bind("name", name)
        .map { row, metadata ->
            Currency(row.get("c_id", Long::class.java), row.get("c_name", String::class.java)!!,
                CurrencyType(row.get("ct_id", Long::class.java), row.get("ct_name", String::class.java)!!)
            )
        }
        .one()

    override fun save(currency: Currency): Mono<Currency> =
        databaseClient.sql("INSERT INTO currency (name, type_id) VALUES (:name, :type_id) RETURNING id")
            .bind("name", currency.name)
            .bind("type_id", currency.type.id!!)
            .flatMap { it ->
                it.map { row, metadata ->
                    Currency(row.get("id", Long::class.java), currency.name, currency.type)
                }
            }.next()

    override fun findCurrencyByType(currencyType: CurrencyType): Flux<Currency> =
        databaseClient.sql("SELECT * from currency c where c.type_id = :type_id")
            .bind("type_id", currencyType.id!!)
            .map { row, metadata ->
                Currency(row.get("id", Long::class.java), row.get("name", String::class.java)!!,
                    currencyType)
            }.all()

    override fun deleteAll(): Mono<Void> =
        databaseClient.sql("DELETE FROM currency")
            .then()
}