package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.Currency
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface CurrencyRepository: ReactiveCrudRepository<Currency, Long> {
}