package apavliuk.currencyrateservice.controller;

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse;
import apavliuk.currencyrateservice.service.CurrenciesService;
import com.ninjasquad.springmockk.MockkBean;
import io.mockk.MockKAnnotations;
import io.mockk.impl.annotations.InjectMockKs;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(CurrencyRateController.class)
public class CurrencyRateControllerTest {
    private final String CONTROLLER_URL = "/currency-rates";

    @MockitoBean
    CurrenciesService currenciesService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void shouldReturnCurrencies() {
        var data = new CurrenciesRateResponse(
                List.of(),
                List.of()
        );
        Mockito.when(currenciesService.requestCurrencies())
                .thenReturn(Mono.just(data));

        webTestClient
            .get()
            .uri(CONTROLLER_URL)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType("application/json")
            .expectBody(CurrenciesRateResponse.class);
    }
}