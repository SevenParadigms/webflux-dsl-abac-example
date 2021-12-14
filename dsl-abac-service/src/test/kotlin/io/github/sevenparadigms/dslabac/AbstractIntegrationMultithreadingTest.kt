package io.github.sevenparadigms.dslabac

import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.abac.security.context.ExchangeContext
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.NetworkInterface

@TestPropertySource("classpath:application.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationMultithreadingTest {

    @LocalServerPort
    protected var port = 0
    protected val countUsersForTest = 100
    protected val correctIp = "192.168.2.207"
    protected val nonCorrectIp = "127.0.0.1"
    protected val testUsersPassword = "passwore"

    @Autowired
    protected lateinit var exchangeContext: ExchangeContext

    protected lateinit var databaseClient: DatabaseClient
    protected lateinit var webClient: WebClient
    protected lateinit var adminToken: String
    protected lateinit var userToken: String
    protected lateinit var host: String

    @Value("\${spring.security.abac.url}")
    private lateinit var databaseUrl: String

    @BeforeAll
    fun setup() {
        runBlocking {
            host = NetworkInterface.getNetworkInterfaces().asIterator().next().inetAddresses.asIterator()
                .next().hostAddress
            webClient = WebClient.builder().clientConnector(ReactorClientHttpConnector())
                .baseUrl("http://$host:$port/").build()
            adminToken = getAwaitToken("admin")
            userToken = getAwaitToken("user")
            databaseClient = DatabaseClient.create(ConnectionFactories.get(databaseUrl))

            val userQueryBuffer = StringBuffer("insert into local_user(login, password) values")
            val authorityQueryBuffer = StringBuffer("insert into authority_user(authority_id, user_id) values")

            Flux.range(0, countUsersForTest)
                .parallel(10)
                .runOn(Schedulers.boundedElastic())
                .flatMap {
                    userQueryBuffer.append("('test$it', 'b9y3FIltPBbk7rrP80Tav8CTHRBRfg=='),")
                    authorityQueryBuffer.append("(getAuthority('ROLE_USER'), getUser('test$it')),")
                    Flux.just(it)
                }
                .doOnComplete {
                    runBlocking {
                        databaseClient.sql(userQueryBuffer.substring(0, userQueryBuffer.length - 1) + ";").fetch()
                            .rowsUpdated().awaitLast()
                    }
                    databaseClient.sql(authorityQueryBuffer.substring(0, authorityQueryBuffer.length - 1) + ";").fetch()
                        .rowsUpdated().subscribe()
                }
                .awaitLast()
        }
    }

    @AfterAll
    fun shutdown() {
        databaseClient.sql("delete from local_user where login like 'test%';").fetch().rowsUpdated()
            .subscribe()
    }

    protected suspend fun getAwaitToken(login: String): String {
        return webClient.post()
            .uri("auth")
            .body(BodyInserters.fromPublisher(Mono.just(createUser(login)), UserPrincipal::class.java))
            .retrieve()
            .awaitBody()
    }

    protected fun createUser(login: String): UserPrincipal {
        return UserPrincipal(
            login = login,
            password = testUsersPassword
        )
    }

    protected fun getToken(login: String): Mono<String> {
        return webClient.post()
            .uri("auth")
            .body(BodyInserters.fromPublisher(Mono.just(createUser(login)), UserPrincipal::class.java))
            .exchangeToMono { resp -> resp.bodyToMono(String::class.java) }
    }
}