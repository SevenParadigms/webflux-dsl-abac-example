package io.github.sevenparadigms.dslabac.testing

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.abac.security.context.ExchangeContext
import io.github.sevenparadigms.dslabac.data.FolderRepository
import io.github.sevenparadigms.dslabac.testing.config.PostgresTestContainer
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono
import java.net.NetworkInterface
import java.util.*

abstract class AbstractIntegrationTest : PostgresTestContainer() {

    @LocalServerPort
    protected var port = 0
    protected val correctIp = "192.168.2.207"
    protected val nonCorrectIp = "127.0.0.1"
    protected val testUsersPassword = "passwore"
    protected var jfolderId: UUID? = null
    protected var jobjectId: UUID? = null

    @Autowired
    protected lateinit var folderRepository: FolderRepository

    @Autowired
    protected lateinit var exchangeContext: ExchangeContext

    protected lateinit var webClient: WebClient
    protected lateinit var adminToken: String
    protected lateinit var userToken: String
    protected lateinit var host: String
    protected lateinit var objectMapper: ObjectMapper

    @BeforeAll
    fun setup() {
        runBlocking {
            objectMapper = ObjectMapper()
            host = NetworkInterface.getNetworkInterfaces().asIterator().next().inetAddresses.asIterator()
                .next().hostAddress
            webClient = WebClient.builder().clientConnector(ReactorClientHttpConnector())
                .baseUrl("http://$host:$port/").build()
            adminToken = getToken("admin")
            userToken = getToken("user")
            jfolderId = folderRepository.findFolderIdByJtreeName("organization").awaitFirst()
        }
    }

    protected suspend fun getToken(login: String): String {
        return webClient.post()
            .uri("auth")
            .body(BodyInserters.fromPublisher(Mono.just(createUser(login)), UserPrincipal::class.java))
            .retrieve()
            .awaitBody()
    }

    private fun createUser(login: String): UserPrincipal {
        return UserPrincipal(
            login = login,
            password = testUsersPassword
        )
    }
}