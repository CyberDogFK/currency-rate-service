package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.Currency
import reactor.core.publisher.Mono

interface CurrencyRepository {
    fun findCurrencyByName(name: String): Mono<Currency>

    fun save(currency: Currency): Mono<Currency>
}