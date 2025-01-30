package apavliuk.currencyrateservice.dto

data class CurrenciesRateResponse(
    val fiat: List<CurrenciesWebServiceResponse>,
    val crypto: List<CurrenciesWebServiceResponse>
)
