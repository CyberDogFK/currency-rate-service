package apavliuk.currencyrateservice.service

import apavliuk.currencyrateservice.service.impl.CurrenciesRateResponse
import reactor.core.publisher.Mono

interface CurrenciesService {
    fun requestCurrencies(): Mono<CurrenciesRateResponse>
}