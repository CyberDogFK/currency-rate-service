package apavliuk.currencyrateservice;

import apavliuk.currencyrateservice.dto.CurrenciesRateResponse;
import apavliuk.currencyrateservice.dto.CurrenciesWebServiceResponse;
import apavliuk.currencyrateservice.service.impl.CurrenciesRateServiceImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.hamcrest.Matchers;

// Integration tests here
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CurrencyRateServiceApplicationTests {
    private static final String ENDPOINT = "/currency-rates";
    private static final String EXAMPLE_RESPONSE_FIAT = """
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
    """;
    private static final String EXAMPLE_RESPONSE_CRYPTO = """
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
        """;
    private final String fiatPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Fiat.getUrlPath();
    private final String cryptoPath = CurrenciesRateServiceImpl.PreparedCurrencyType.Crypto.getUrlPath();

    private MockWebServer mockWebServer;
//    @MockitoSpyBean
//    private WebClient webClient;
    @Autowired
    private CurrenciesRateServiceImpl currenciesRateService;
    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void init() {
        mockWebServer = new MockWebServer();
        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        ReflectionTestUtils.setField(currenciesRateService, "webClient", webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
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

        var expected = new CurrenciesRateResponse(List.of(), List.of());

        webTestClient.get()
                .uri(ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrenciesRateResponse.class)
                .isEqualTo(expected);
    }

    // add test where only one?

    @Order(3)
    @Test
    void testRequestOkSaveToEmptyDb() {
        mockWebServer.setDispatcher(getDispatcherForTwoPath(
                new MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(EXAMPLE_RESPONSE_FIAT),
                new MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(EXAMPLE_RESPONSE_CRYPTO)
        ));

        webTestClient.get()
                .uri(ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrenciesRateResponse.class)
                .value(this::assertDefaultCurrencies);
    }

    @Order(4)
    @Test
    void testRequestFailShouldReturnFromDb() {
        mockWebServer.setDispatcher(getDispatcherForTwoPath(
                new MockResponse().setResponseCode(500),
                new MockResponse().setResponseCode(500)
        ));

        webTestClient.get()
                .uri(ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrenciesRateResponse.class)
                .value(this::assertDefaultCurrencies);
    }

    private Dispatcher getDispatcherForTwoPath(MockResponse fiatResponse, MockResponse cryptoResponse) {
        return new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                if (recordedRequest.getPath().equals(fiatPath)) {
                    return fiatResponse;
                } else if (recordedRequest.getPath().equals(cryptoPath)) {
                    return cryptoResponse;
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        };
    }

    private void assertDefaultCurrencies(CurrenciesRateResponse result) {
        Assertions.assertEquals(result.getFiat().size(), 2);
        Assertions.assertEquals(result.getCrypto().size(), 2);
        var fiatCurrencies = result.getFiat().stream()
                .collect(Collectors.toMap(
                        CurrenciesWebServiceResponse::getCurrency,
                        CurrenciesWebServiceResponse::getRate));
        Assertions.assertEquals(fiatCurrencies.get("USD"), BigDecimal.valueOf(45.67));
        Assertions.assertEquals(fiatCurrencies.get("EUR"), BigDecimal.valueOf(56.78));
        var cryptoCurrencies = result.getCrypto().stream()
                .collect(Collectors.toMap(
                        CurrenciesWebServiceResponse::getCurrency,
                        CurrenciesWebServiceResponse::getRate
                ));
        Assertions.assertEquals(cryptoCurrencies.get("BTC"), BigDecimal.valueOf(12345.67));
        Assertions.assertEquals(cryptoCurrencies.get("ETH"), BigDecimal.valueOf(234.56));
    }

}
