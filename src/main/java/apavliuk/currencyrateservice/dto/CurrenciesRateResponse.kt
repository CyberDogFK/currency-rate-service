package apavliuk.currencyrateservice.dto

class CurrenciesRateResponse(
    val fiat: List<CurrenciesWebServiceResponse>,
    val crypto: List<CurrenciesWebServiceResponse>
)
