package apavliuk.currencyrateservice.repository;

import apavliuk.currencyrateservice.model.Currency;
import apavliuk.currencyrateservice.model.CurrencyType;
import apavliuk.currencyrateservice.model.HistoricalRate;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class HistoricalRateRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgreSQLContainer.getHost() + ":" +
                        postgreSQLContainer.getFirstMappedPort() + "/" +
                        postgreSQLContainer.getDatabaseName()
        );
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);
        registry.add("spring.liquibase.url", () ->
                "jdbc:postgresql://" + postgreSQLContainer.getHost() + ":" +
                        postgreSQLContainer.getFirstMappedPort() + "/" +
                        postgreSQLContainer.getDatabaseName()
        );
        registry.add("spring.liquibase.user", postgreSQLContainer::getUsername);
        registry.add("spring.liquibase.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private HistoricalRateRepository historicalRateRepository;
    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void init() {
        postgreSQLContainer.start();
    }

    @AfterEach
    void clean() {
        historicalRateRepository.deleteAll().block();
        currencyRepository.deleteAll().block();
    }

    @Test
    void testSaveQueryFailWithoutSavedCurrency() {
        var test = new HistoricalRate(null,
                new Currency(1L, "USD", new CurrencyType(1L, "fiat")),
                1L, BigDecimal.ONE);

        var task = historicalRateRepository.save(test);

        StepVerifier.create(task)
                .verifyError();
    }

    @Test
    void testSaveQueryWithSavedCurrency() {
        var fiat = new CurrencyType(1L, "fiat");
        var task = currencyRepository.save(new Currency(null, "USD", fiat))
                .flatMap(usd ->
                        historicalRateRepository.save(
                                new HistoricalRate(null, usd, 1L, BigDecimal.TEN))
                );

        StepVerifier.create(task)
                .consumeNextWith(r -> {
                    Assertions.assertEquals(1L, r.getId());
                    Assertions.assertEquals("USD", r.getCurrency().getName());
                    Assertions.assertEquals(fiat, r.getCurrency().getType());
                    Assertions.assertEquals(BigDecimal.TEN, r.getRate());
                }).verifyComplete();
    }

    @Test
    void testFindLastRateForCurrency() {
        var crypto = new CurrencyType(2L, "crypto");

        var btc = currencyRepository.save(new Currency(null, "BTC", crypto)).block();
        historicalRateRepository.save(new HistoricalRate(null, btc, 1L, BigDecimal.TEN)).block();
        historicalRateRepository.save(
                new HistoricalRate(null, btc, 2L, BigDecimal.ONE)).block();
        historicalRateRepository.save(
                new HistoricalRate(null, btc, 3L, BigDecimal.TWO)).block();
        var rate = historicalRateRepository.findLastRateForCurrency(
                new Currency(1L, "BTC", crypto)).block();

        Assertions.assertNotNull(rate.getId());
        Assertions.assertEquals(crypto, rate.getCurrency().getType());
        Assertions.assertEquals(BigDecimal.TWO, rate.getRate());
    }
}