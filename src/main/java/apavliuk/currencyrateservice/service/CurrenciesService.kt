package apavliuk.currencyrateservice.service

import reactor.core.publisher.Mono

interface CurrenciesService {
    fun requestCurrencies(): Mono<String>
}