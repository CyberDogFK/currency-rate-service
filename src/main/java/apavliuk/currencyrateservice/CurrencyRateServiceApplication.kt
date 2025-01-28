package apavliuk.currencyrateservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CurrencyRateServiceApplication

fun main(args: Array<String>) {
    runApplication<CurrencyRateServiceApplication>(*args)
}
