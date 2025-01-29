package apavliuk.currencyrateservice.service

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse
import reactor.core.publisher.Mono

interface CurrenciesRateService {
    fun requestCurrencies(): Mono<CurrenciesRateResponse>
}