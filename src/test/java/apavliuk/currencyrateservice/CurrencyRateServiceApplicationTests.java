package apavliuk.currencyrateservice;

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;

// Integration tests here
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@RunWith(SpringRunner.class)
class CurrencyRateServiceApplicationTests {
    //
//    @Value("\${currencies-service.url}")
//    private lateinit var serviceUrl: String
//
//    @Value("\${currencies-service.api-key}")
//    private var apiKey: String? = null
//
//    @Bean
//    fun getWebClient(): WebClient =
//            WebClient.builder()
//            .baseUrl(serviceUrl)
//            .defaultHeader("X-API-KEY", apiKey)
//            .build()

    private MockWebServer mockWebServer;
    @MockitoSpyBean
    private WebClient webClient;
    @Autowired
    private WebTestClient webTestClient;


    @BeforeEach
    void init() {
        mockWebServer = new MockWebServer();
        webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
    }

    @Order(1)
    @Test
    void contextLoads() {
    }

    @Order(2)
    @Test
    void testRequest505EmptyDb() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        webTestClient.get()
                .uri("/currency-rates")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrenciesRateResponse.class);
    }
}
