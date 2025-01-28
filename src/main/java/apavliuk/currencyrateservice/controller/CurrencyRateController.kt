package apavliuk.currencyrateservice.controller

import apavliuk.currencyrateservice.service.CurrenciesService
import apavliuk.currencyrateservice.service.impl.CurrenciesRateResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/currency-rates")
class CurrencyRateController(
    private val currenciesService: CurrenciesService
) {
    // /currency-rates
    @GetMapping
    fun getCurrencyRate(): Mono<CurrenciesRateResponse> {
        return currenciesService.requestCurrencies()
    }
}
