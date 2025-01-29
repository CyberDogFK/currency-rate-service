package apavliuk.currencyrateservice.controller

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse
import apavliuk.currencyrateservice.service.CurrenciesService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/currency-rates")
class CurrencyRateController(
    private val currenciesService: CurrenciesService
) {
    @GetMapping
    fun getCurrencyRate(): Mono<CurrenciesRateResponse> {
        return currenciesService.requestCurrencies()
    }
}
