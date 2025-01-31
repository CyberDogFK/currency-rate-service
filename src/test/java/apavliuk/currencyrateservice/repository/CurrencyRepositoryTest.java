package apavliuk.currencyrateservice.repository;

import apavliuk.currencyrateservice.model.Currency;
import apavliuk.currencyrateservice.model.CurrencyType;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CurrencyRepositoryTest {
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
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void init() {
        postgreSQLContainer.start();
    }

    @AfterEach
    void clean() {
        currencyRepository.deleteAll().block();
    }


    @Test
    void testSaveQuery() {
        var currency = new Currency(null, "USD",
                new CurrencyType(1L, "fiat"));

        var data = currencyRepository.save(currency);

        StepVerifier.create(data)
                .consumeNextWith(p -> {
                            Assertions.assertEquals(1L, p.getId());
                            Assertions.assertEquals("USD", p.getName());
                        }
                )
                .verifyComplete();
    }

    @Test
    void testFindByNameQuery() {
        var currencyUsd = new Currency(null, "USD",
                new CurrencyType(1L, "fiat"));
        var currencyEur = new Currency(null, "EUR",
                new CurrencyType(1L, "fiat"));

        var t1 = currencyRepository.save(currencyUsd);
        var t2 = currencyRepository.save(currencyEur);
        var zip = Mono.zip(t1, t2);
        StepVerifier.create(zip)
                .expectNextCount(1)
                .verifyComplete();

        var result = currencyRepository.findCurrencyByName("USD");

        StepVerifier.create(result)
                .consumeNextWith(r -> {
                    Assertions.assertNotNull(r.getId());
                    Assertions.assertEquals("USD", r.getName());
                    Assertions.assertEquals("fiat", r.getType().getName());
                }).verifyComplete();

        result = currencyRepository.findCurrencyByName("EUR");
        StepVerifier.create(result)
                .consumeNextWith(r -> {
                    Assertions.assertNotNull(r.getId());
                    Assertions.assertEquals("EUR", r.getName());
                    Assertions.assertEquals("fiat", r.getType().getName());
                }).verifyComplete();
    }

    @Test
    void testFindByTypeQuery() {
        var fiatType = new CurrencyType(1L, "fiat");
        var cryptoType = new CurrencyType(2L, "crypto");
        var currencyUsd = new Currency(null, "USD", fiatType);
        var currencyEur = new Currency(null, "BTC", cryptoType);

        var t1 = currencyRepository.save(currencyUsd);
        var t2 = currencyRepository.save(currencyEur);
        var zip = Mono.zip(t1, t2);
        StepVerifier.create(zip)
                .expectNextCount(1)
                .verifyComplete();

        var result = currencyRepository.findCurrencyByType(fiatType);
        StepVerifier.create(result)
                .consumeNextWith(r -> {
                    Assertions.assertNotNull(r.getId());
                    Assertions.assertEquals(fiatType, r.getType());
                    Assertions.assertEquals("USD", r.getName());
                }).verifyComplete();

        result = currencyRepository.findCurrencyByType(cryptoType);
        StepVerifier.create(result)
                .consumeNextWith(r -> {
                    Assertions.assertNotNull(r.getId());
                    Assertions.assertEquals(cryptoType, r.getType());
                    Assertions.assertEquals("BTC", r.getName());
                }).verifyComplete();
    }
}