package io.github.sevenparadigms.dslabac.api.context

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.abac.security.context.ExchangeContext
import io.github.sevenparadigms.dslabac.data.FolderRepository
import io.github.sevenparadigms.dslabac.data.Jobject
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.junit.FixMethodOrder
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.NetworkInterface
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExchangeContextIntegrationTest {

    @LocalServerPort
    private var port = 0
    private val correctIp = "192.168.2.207"
    private val testUsersPassword = "passwore"
    private var jfolderId: UUID? = null

    @Autowired
    private lateinit var folderRepository: FolderRepository

    @Autowired
    private lateinit var exchangeContext: ExchangeContext

    private lateinit var webClient: WebClient
    private lateinit var adminToken: String
    private lateinit var host: String

    @BeforeAll
    fun setup() {
        runBlocking {
            host = NetworkInterface.getNetworkInterfaces().asIterator().next().inetAddresses.asIterator()
                .next().hostAddress
            webClient = WebClient.builder().clientConnector(ReactorClientHttpConnector())
                .baseUrl("http://$host:$port/").build()
            adminToken = getToken("admin")
            jfolderId = folderRepository.findFolderIdByJtreeName("organization").awaitFirst()
        }
    }

    @Test
    fun getRemoteIp() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { exchangeContext.getRemoteIp("admin") == correctIp }
            .thenCancel()
            .verify()
    }

    @Test
    fun getHeaders() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches {
                exchangeContext.getHeaders("admin")!![Constants.AUTHORIZE_IP]!!.first() == correctIp &&
                        exchangeContext.getHeaders("admin")!![HttpHeaders.AUTHORIZATION]!!.first() == Constants.BEARER + adminToken
            }
            .thenCancel()
            .verify()
    }

    @Test
    fun getRequest() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { exchangeContext.getRequest("admin") != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun getResponse() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { exchangeContext.getResponse("admin") != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun getSession() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { exchangeContext.getSession("admin") != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun getToken() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { exchangeContext.getToken("admin") == adminToken }
            .thenCancel()
            .verify()
    }

    @Test
    fun getUser() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { exchangeContext.getUser("admin") != null }
            .thenCancel()
            .verify()
    }

    private suspend fun getToken(login: String): String {
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