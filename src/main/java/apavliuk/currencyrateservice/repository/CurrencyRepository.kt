package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CurrencyRepository {
    fun findCurrencyByName(name: String): Mono<Currency>

    fun save(currency: Currency): Mono<Currency>

    fun findLastCurrency(type: CurrencyType): Flux<Currency>
}