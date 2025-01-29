package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.model.CurrencyType
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface CurrencyTypeRepository: ReactiveCrudRepository<CurrencyType, Long> {
}