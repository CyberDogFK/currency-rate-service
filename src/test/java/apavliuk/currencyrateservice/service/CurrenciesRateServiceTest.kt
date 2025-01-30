package apavliuk.currencyrateservice.service

import apavliuk.currencyrateservice.dto.CurrenciesWebServiceResponse
import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import apavliuk.currencyrateservice.service.impl.CurrenciesRateServiceImpl
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
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
import java.net.URI

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

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    val fiatPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Fiat.urlPath
    val cryptoPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Crypto.urlPath

    @BeforeEach
    fun init() {
        val baseHttpClient = HttpClient.create()
            .doOnRequest { request, conn ->
                conn.addHandlerFirst(LoggingHandler(LogLevel.INFO))
            }

        val webClient = WebClient.builder()
            .baseUrl("http://testhost:1111")
            .exchangeFunction(exchangeFunction)
            .clientConnector(ReactorClientHttpConnector(baseHttpClient))
            .filters { exchangeFilterFunctions -> {
            exchangeFilterFunctions.add(logRequest());
//            exchangeFilterFunctions.add(logResponse());
        }}
            .build()

        currenciesRateService = CurrenciesRateServiceImpl(
            webClient,
            currencyRepository,
            historicalRateRepository
        )
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
        val s = object : ArgumentMatcher<ClientRequest> {
            override fun matches(argument: ClientRequest?): Boolean {
                return argument!!.url().path.equals("/fiat-currency-rates")
            }
        }

        val client = ClientRequest.create(HttpMethod.GET, URI.create("http://testhost:1111/fiat-currency-rates")).build()
        Mockito.`when`(exchangeFunction.exchange(
//            any(ClientRequest::class.java)))
            Mockito.argThat { clientr -> clientr.url().path.equals("fiat-currency-rates") }
        ))
            .thenReturn(Mono.just(
                ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(exampleResponseFiat)
                    .build()
            ))

        Mockito.`when`(exchangeFunction
            .exchange(
//            ClientRequest.create(HttpMethod.GET, URI.create("http://testhost:1111$cryptoPath")).build()))
                Mockito.argThat { clientr -> clientr.url().path.equals("crypto-currency-rates") }
            ))
//        ArgumentMatchers.assertArg { it.url().path.equals("/crypto-currency-rates") }))
            .thenReturn(Mono.just(
                ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(exampleResponseCrypto)
                    .build()
            ))


        Mockito.`when`(currencyRepository.findCurrencyByName(any(String::class.java)))
            .thenReturn(Mono.empty<Currency>())

        Mockito.`when`(currencyRepository.findCurrencyByType(any(CurrencyType::class.java)))
            .thenReturn(Flux.empty())

        val result = currenciesRateService.requestCurrencies().block()!!
        logger.info(Mockito.mockingDetails(exchangeFunction).printInvocations())
        Assertions.assertTrue(result.fiat.isNotEmpty(), "Result should not be empty")
        Assertions.assertTrue(result.crypto.isNotEmpty(), "Result should not be empty")
        Assertions.assertEquals(result.fiat.size, 2)
        Assertions.assertEquals(
            result.fiat.find{ it.currency == "USD" }!!.rate,
            45.67
        )
        Assertions.assertEquals(result.crypto.size, 2)
        Assertions.assertEquals(
            result.crypto.find{ it.currency == "BTC" }!!.rate,
            12345.67
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