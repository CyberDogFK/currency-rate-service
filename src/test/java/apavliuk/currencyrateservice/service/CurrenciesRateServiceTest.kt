package apavliuk.currencyrateservice.service

import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.model.HistoricalRate
import apavliuk.currencyrateservice.repository.CurrencyRepository
import apavliuk.currencyrateservice.repository.HistoricalRateRepository
import apavliuk.currencyrateservice.service.impl.CurrenciesRateServiceImpl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
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

        private val fiatPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Fiat.urlPath
        private val cryptoPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Crypto.urlPath
    }

    private lateinit var currenciesRateService: CurrenciesRateServiceImpl
    @Mock
    private lateinit var currencyRepository: CurrencyRepository
    @Mock
    private lateinit var historicalRateRepository: HistoricalRateRepository
    private lateinit var mockWebServer: MockWebServer
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    @BeforeEach
    fun init() {
        mockWebServer = MockWebServer()
        val webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
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

    @Test
    fun testEmptyResponseFromServiceAndDb() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

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
                if (request.path.equals(fiatPath)) {
                    MockResponse().setResponseCode(200)
                        .setBody(exampleResponseFiat)
                        .setHeader("Content-Type", "application/json")
                } else if (request.path.equals(cryptoPath)) {
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
}
