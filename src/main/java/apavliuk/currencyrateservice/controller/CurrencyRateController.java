package apavliuk.currencyrateservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/currency-rates")
public class CurrencyRateController {
    // /currency-rates
    @GetMapping
    public Mono<String> test() {
        return Mono.just("Hello");
    }
}
