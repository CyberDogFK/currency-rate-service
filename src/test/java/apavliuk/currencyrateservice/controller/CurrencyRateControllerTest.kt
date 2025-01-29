package apavliuk.currencyrateservice.controller

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse
import apavliuk.currencyrateservice.dto.CurrenciesWebServiceResponse
import apavliuk.currencyrateservice.service.CurrenciesRateService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@WebFluxTest(CurrencyRateController::class)
class CurrencyRateControllerTest {
    companion object {
        private const val CONTROLLER_URL = "/currency-rates"
    }

    @MockitoBean
    private lateinit var currenciesRateService: CurrenciesRateService

    @Autowired
    private var webTestClient: WebTestClient? = null

    @Test
    fun shouldReturnCurrencies() {
        val data = CurrenciesRateResponse(
            listOf<CurrenciesWebServiceResponse>(),
            listOf<CurrenciesWebServiceResponse>()
        )
        Mockito.`when`(currenciesRateService.requestCurrencies())
            .thenReturn(Mono.just<CurrenciesRateResponse>(data))

        webTestClient!!
            .get()
            .uri(CONTROLLER_URL)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType("application/json")
            .expectBody(CurrenciesRateResponse::class.java)
    }
}