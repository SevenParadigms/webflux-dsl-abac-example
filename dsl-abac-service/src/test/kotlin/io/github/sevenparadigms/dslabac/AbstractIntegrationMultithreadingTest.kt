package io.github.sevenparadigms.dslabac

import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.abac.security.context.ExchangeContext
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.runBlocking
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
import reactor.core.publisher.Mono
import java.net.NetworkInterface

@TestPropertySource("classpath:application.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationMultithreadingTest {

    @LocalServerPort
    protected var port = 0
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
            adminToken = getToken("admin")
            userToken = getToken("user")
            databaseClient = DatabaseClient.create(ConnectionFactories.get(databaseUrl))
        }
    }

    protected suspend fun getToken(login: String): String {
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
}