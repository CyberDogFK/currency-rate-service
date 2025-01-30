package apavliuk.currencyrateservice.service

import apavliuk.currencyrateservice.dto.CurrenciesWebServiceResponse
import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.model.HistoricalRate
import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import apavliuk.currencyrateservice.service.impl.CurrenciesRateServiceImpl
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
//@WebFluxTest(CurrenciesRateServiceImpl::class, Web)
//@AutoConfigureWebTestClient
class CurrenciesRateServiceTest {
    companion object {
        // GET `/fiat-currency-rates`
        private val exampleResponseFiat: String = """
        [
          {
            "currency": "USD",
            "rate": 45.67
          },
          {
            "currency": "EUR",
            "rate": 56.78
          }
        ]
    """.trimIndent()

        // GET `/crypto-currency-rates`
        private val exampleResponseCrypto: String = """
            [
              {
                "name": "BTC",
                "value": 12345.67
              },
              {
                "name": "ETH",
                "value": 234.56
              }
            ]
        """.trimIndent()

        private val logger = LoggerFactory.getLogger(CurrenciesRateServiceImpl::class.java)
    }

    private lateinit var currenciesRateService: CurrenciesRateServiceImpl

    @Mock
    private lateinit var currencyRepository: CurrencyRepository
    @Mock
    private lateinit var historicalRateRepository: HistoricalRateRepository

    @Mock
    private lateinit var exchangeFunction: ExchangeFunction

    private val mockWebServer: MockWebServer = MockWebServer()


    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    val fiatPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Fiat.urlPath
    val cryptoPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Crypto.urlPath

    @BeforeEach
    fun init() {
//        val baseHttpClient = HttpClient.create()
//            .doOnRequest { request, conn ->
//                conn.addHandlerFirst(LoggingHandler(LogLevel.INFO))
//            }

        val webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
//            .exchangeFunction(exchangeFunction)
//            .clientConnector(ReactorClientHttpConnector(baseHttpClient))
//            .filters { exchangeFilterFunctions -> {
//            exchangeFilterFunctions.add(logRequest());
////            exchangeFilterFunctions.add(logResponse());
//        }}
            .build()

        currenciesRateService = CurrenciesRateServiceImpl(
            webClient,
            currencyRepository,
            historicalRateRepository
        )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    fun logRequest(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor{clientRequest ->
            if (true) {
                val sb = StringBuilder("Request: \n")
                //append clientRequest method and url
                clientRequest
                    .headers()
                    .forEach{(name, values) -> values.forEach{value -> /* append header key/value */}}
                logger.debug(sb.toString())
            }
            Mono.just(clientRequest);
        }
    }

    @Test
    fun testEmptyResponseFromServiceAndDb() {
        val fiatPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Fiat.urlPath
        val cryptoPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Crypto.urlPath
//
        Mockito.`when`(exchangeFunction.exchange(Mockito.any(ClientRequest::class.java)))
            .thenReturn(Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                .build()))

        Mockito.`when`(currencyRepository.findCurrencyByName(any(String::class.java)))
            .thenReturn(Mono.empty<Currency>())

        Mockito.`when`(currencyRepository.findCurrencyByType(any(CurrencyType::class.java)))
            .thenReturn(Flux.empty())

        val result = currenciesRateService.requestCurrencies().block()!!
        Assertions.assertTrue(result.fiat.isEmpty(), "Result should be empty")
        Assertions.assertTrue(result.crypto.isEmpty(), "Result should be empty")
    }

    @Test
    fun testServiceGetResponseFromServiceEmptyDB() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (request.path.equals("/fiat-currency-rates")) {
                    MockResponse().setResponseCode(200)
                        .setBody(exampleResponseFiat)
                        .setHeader("Content-Type", "application/json")
                } else if (request.path.equals("/crypto-currency-rates")) {
                    MockResponse().setResponseCode(200)
                        .setBody(exampleResponseCrypto)
                        .setHeader("Content-Type", "application/json")
                } else {
                    MockResponse().setResponseCode(404)
                }
        }

        val fiatType = CurrencyType(1, "fiat")
        val cryptoType = CurrencyType(2, "crypto")
        val usd = Currency(1, "USD", fiatType)
        val eur = Currency(2, "EUR", fiatType)
        val btc = Currency(3, "BTC", cryptoType)
        val eth = Currency(4, "ETH", cryptoType)
        Mockito.`when`(currencyRepository.findCurrencyByName(any(String::class.java)))
            .thenReturn(Mono.empty<Currency>())
        Mockito.`when`(currencyRepository.findCurrencyByType(any(CurrencyType::class.java)))
            .thenReturn(Flux.empty())
        Mockito.`when`(currencyRepository.save(Currency(null, "USD", fiatType)))
            .thenReturn(Mono.just(usd))
        Mockito.`when`(currencyRepository.save(Currency(null, "EUR", fiatType)))
            .thenReturn(Mono.just(eur))
        Mockito.`when`(currencyRepository.save(Currency(null, "BTC", cryptoType)))
            .thenReturn(Mono.just(btc))
        Mockito.`when`(currencyRepository.save(Currency(null, "ETH", cryptoType)))
            .thenReturn(Mono.just(eth))

        val localDateTime: MockedStatic<LocalDateTime> = Mockito.mockStatic<LocalDateTime>(LocalDateTime::class.java)
//        val zonedDateTime: MockedStatic<ZonedDateTime> = Mockito.mockStatic<ZonedDateTime>(ZonedDateTime::class.java)
        val mockLocalDateTime = Mockito.mock(LocalDateTime::class.java)
        val mockZonedDateTime = Mockito.mock(ZonedDateTime::class.java)
        localDateTime.`when`<LocalDateTime> { LocalDateTime.now() }.thenReturn(mockLocalDateTime)
        Mockito.`when`(mockLocalDateTime.atZone(ZoneId.systemDefault())).thenReturn(mockZonedDateTime)
        Mockito.`when`(mockZonedDateTime.toEpochSecond()).thenReturn(1)


        Mockito.`when`(historicalRateRepository.save(HistoricalRate(null, usd, 1L, BigDecimal.valueOf(45.67))))
            .thenReturn(Mono.just(HistoricalRate(1, usd, 1, BigDecimal.valueOf(45.67))))
        Mockito.`when`(historicalRateRepository.save(HistoricalRate(null, eur, 1L, BigDecimal.valueOf(56.78))))
            .thenReturn(Mono.just(HistoricalRate(2, eur, 1, BigDecimal.valueOf(56.78))))
        Mockito.`when`(historicalRateRepository.save(HistoricalRate(null, btc, 1L, BigDecimal.valueOf(12345.67))))
            .thenReturn(Mono.just(HistoricalRate(3, btc, 1, BigDecimal.valueOf(12345.67))))
        Mockito.`when`(historicalRateRepository.save(HistoricalRate(null, eth, 1L, BigDecimal.valueOf(234.56))))
            .thenReturn(Mono.just(HistoricalRate(4, eth, 1, BigDecimal.valueOf(234.56))))
//        Mockito.`when`(historicalRateRepository.save(any(HistoricalRate::class.java)))
//            .thenReturn(Mono.just(HistoricalRate(0, usd, 0, BigDecimal.ZERO)))

        val result = currenciesRateService.requestCurrencies().block()!!
        logger.info(Mockito.mockingDetails(historicalRateRepository).printInvocations())
        Assertions.assertTrue(result.fiat.isNotEmpty(), "Result should not be empty")
        Assertions.assertTrue(result.crypto.isNotEmpty(), "Result should not be empty")
        Assertions.assertEquals(result.fiat.size, 2)
        Assertions.assertEquals(
            result.fiat.find{ it.currency == "USD" }!!.rate,
            BigDecimal.valueOf(45.67)
        )
        Assertions.assertEquals(result.crypto.size, 2)
        Assertions.assertEquals(
            result.crypto.find{ it.currency == "BTC" }!!.rate,
            BigDecimal.valueOf(12345.67)
        )
    }

    //     - **Error (401):** Invalid API key
    //  ```json
    //    {
    //      "error": "Invalid API key"
    //    }
    //    ```
    //    - **Error (500):** Internal Server Error (20% chance)
    //  ```json
    //    {
    //      "error": "Internal Server Error"
    //    }


}