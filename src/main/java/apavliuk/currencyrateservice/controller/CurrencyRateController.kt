package apavliuk.currencyrateservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/currency-rates")
class CurrencyRateController {
    // /currency-rates
    @GetMapping
    fun test(): Mono<String> = Mono.just<String>("Hello\n")
}
