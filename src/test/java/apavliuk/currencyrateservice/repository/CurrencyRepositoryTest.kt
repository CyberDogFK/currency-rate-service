package apavliuk.currencyrateservice.repository

import apavliuk.currencyrateservice.controller.CurrencyRateController
import apavliuk.currencyrateservice.model.Currency
import apavliuk.currencyrateservice.model.CurrencyType
import apavliuk.currencyrateservice.repository.impl.CurrencyRepositoryImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier

@Testcontainers
@DataR2dbcTest()
//@WebFluxTest(CurrencyRepositoryImpl::class)
//@ExtendWith(SpringExtension::class)
//@RunWith(SpringRunner::class)
class CurrencyRepositoryTest {
    companion object {
        @JvmStatic
        @Container
        val postgreSQLContainer = PostgreSQLContainer("postgres:latest")

        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/${postgreSQLContainer.databaseName}"
            }
            registry.add("spring.r2dbc.username") {
                postgreSQLContainer.username
            }
            registry.add("spring.r2dbc.password") {
                postgreSQLContainer.password
            }
            registry.add("spring.liquibase.url") {
                "r2dbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/${postgreSQLContainer.databaseName}"
            }
            registry.add("spring.liquibase.user") {
                postgreSQLContainer.username
            }
            registry.add("spring.liquibase.password") {
                postgreSQLContainer.password
            }
        }
    }

    @Autowired
    private var currencyRepository: CurrencyRepositoryImpl? = null

    @BeforeEach
    fun init() {
        postgreSQLContainer.start()
    }

    @Test
    fun testSaveQuery() {
        val currency = Currency(null, "USD", CurrencyType(1, "fiat"))
        val expected = Currency(1, "USD", CurrencyType(1, "fiat"))

        val data = currencyRepository!!.save(currency)

        StepVerifier.create(data)
            .consumeNextWith { p ->
                Assertions.assertEquals(p.id, 1L)
            }
            .verifyComplete()
    }
    //     @Test
    //    public void testQueryByExample() {
    //        var post = Post.builder().title("r2dbc").build();
    //        var exampleMatcher = ExampleMatcher.matching().withMatcher("title", matcher -> matcher.ignoreCase().contains());
    //        var example = Example.of(post, exampleMatcher);
    //        var data = posts.findBy(example, postReactiveFluentQuery -> postReactiveFluentQuery.page(PageRequest.of(0, 10)));
    //
    //        StepVerifier.create(data)
    //                .consumeNextWith(p -> {
    //                    log.debug("post data: {}", p.getContent());
    //                    assertThat( p.getTotalElements()).isEqualTo(1);
    //                })
    //                .verifyComplete();
    //    }
}