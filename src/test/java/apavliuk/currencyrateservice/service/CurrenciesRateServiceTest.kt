package apavliuk.currencyrateservice.service

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import apavliuk.currencyrateservice.service.impl.CurrenciesRateServiceImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@WebFluxTest
class CurrenciesRateServiceTest {
//    @InjectMocks
    private lateinit var currenciesRateService: CurrenciesRateServiceImpl

//    @Mock
//    private lateinit var webClient: WebClient
    @Mock
    private lateinit var currencyRepository: CurrencyRepository
    @Mock
    private lateinit var historicalRateRepository: HistoricalRateRepository

    @Mock
    private lateinit var exchangeFunction: ExchangeFunction

    @BeforeEach
    fun init() {
        val webClient = WebClient.builder()
            .exchangeFunction(exchangeFunction)
            .build()

        currenciesRateService = CurrenciesRateServiceImpl(
            webClient,
            currencyRepository,
            historicalRateRepository
        )
    }

    @Test
    fun testEmptyResponseFromServiceAndDb() {
        val fiatPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Fiat.urlPath
        val cryptoPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Crypto.urlPath

        Mockito.`when`(exchangeFunction.exchange(Mockito.any(ClientRequest::class.java)))
            .thenReturn(Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                .build()))

        Mockito.`when`(currencyRepository.findCurrencyByName(Mockito.any()))
            .thenReturn(Mono.empty<Currency>())

        val result = currenciesRateService.requestCurrencies().block()!!
        Assertions.assertTrue(result.fiat.isEmpty(), "Result should be empty")
        Assertions.assertTrue(result.crypto.isEmpty(), "Result should be empty")
    }
}