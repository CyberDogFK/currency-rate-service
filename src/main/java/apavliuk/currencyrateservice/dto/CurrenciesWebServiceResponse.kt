package apavliuk.currencyrateservice.dto

import com.fasterxml.jackson.annotation.JsonAlias
import java.math.BigDecimal

data class CurrenciesWebServiceResponse(
    @JsonAlias("currency", "name")
    val currency: String,
    @JsonAlias("rate", "value")
    val rate: BigDecimal
)
