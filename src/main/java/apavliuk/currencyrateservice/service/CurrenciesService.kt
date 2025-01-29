package apavliuk.currencyrateservice.service

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse
import reactor.core.publisher.Mono

interface CurrenciesService {
    fun requestCurrencies(): Mono<CurrenciesRateResponse>
}