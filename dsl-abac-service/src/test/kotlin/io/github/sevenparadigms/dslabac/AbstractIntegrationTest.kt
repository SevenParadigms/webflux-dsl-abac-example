package io.github.sevenparadigms.dslabac

import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.abac.security.context.ExchangeContext
import io.github.sevenparadigms.dslabac.data.FolderRepository
import io.github.sevenparadigms.dslabac.feature.FeatureRepository
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.sevenparadigms.kotlin.common.objectToJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono
import java.util.*

@TestPropertySource("classpath:application-jwt.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest : PostgresTestContainer() {

    @LocalServerPort
    protected var port = 55555
    protected val correctIp = "192.168.2.207"
    protected val nonCorrectIp = "127.0.0.1"
    protected val testUsersPassword = "passwore"
    protected var jfolderId: UUID? = null

    @Autowired
    protected lateinit var folderRepository: FolderRepository

    @Autowired
    protected lateinit var exchangeContext: ExchangeContext

    @Autowired
    protected lateinit var featureRepository: FeatureRepository

    protected lateinit var webClient: WebClient
    protected lateinit var adminToken: String
    protected lateinit var userToken: String

    @BeforeAll
    fun setup() {
        runBlocking {
            webClient = WebClient.builder().clientConnector(ReactorClientHttpConnector())
                .baseUrl("http://localhost:$port/").build()
            adminToken = getToken("admin").objectToJson().get("access_token").asText()
            userToken = getToken("user").objectToJson().get("access_token").asText()
            jfolderId = folderRepository.findFolderIdByJtreeName("organization").awaitFirst()
        }
    }

    protected suspend fun getToken(login: String): String {
        return webClient.post()
            .uri("token")
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