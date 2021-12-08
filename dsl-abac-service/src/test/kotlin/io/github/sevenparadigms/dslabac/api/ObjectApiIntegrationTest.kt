package io.github.sevenparadigms.dslabac.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.dslabac.data.Jfolder
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.dto.JobjectDto
import kotlinx.coroutines.runBlocking
import org.junit.FixMethodOrder
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runners.MethodSorters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.NetworkInterface
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ObjectApiIntegrationTest {

    @LocalServerPort
    private var port = 0
    private var jobjectId: UUID? = null
    private val correctIp = "192.168.2.207"
    private val nonCorrectIp = "127.0.0.1"
    private val testUsersPassword =
        "+vlAkdr5kgSo1jbsCx/Moxg19SabHuiNo1eMw58GwGW4cDNrSgfudpi9xTyHH8iYDFMb54uWNjYwIglfY5OYMQJWxebjSG2wY2XF+EIp7/oTsWYEYrWycPiD"

    private lateinit var objectMapper: ObjectMapper
    private lateinit var webClient: WebClient
    private lateinit var adminToken: String
    private lateinit var userToken: String
    private lateinit var host: String

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
        }
    }

    @Test
    fun aSave() {
        val jobject = JobjectDto(
            jtree = objectMapper.readTree("{\"name\": \"Testin123g\", \"description\": \"Testing\"}"),
            jfolder = Jfolder(jtree = objectMapper.readTree("{\"name\": \"Organization\"}"))
        )

        val saveResponse = webClient.post()
            .uri("dsl-abac")
            .header("Authorization", "Bearer $adminToken")
            .body(BodyInserters.fromPublisher(Mono.just(jobject), JobjectDto::class.java))
            .retrieve()
            .bodyToMono(Jobject::class.java)
            .doOnNext { jobjectId = it.id }

        StepVerifier.create(saveResponse)
            .expectSubscription()
            .expectNextMatches { it != null }
            .verifyComplete()
    }

    @Test
    fun bFindAll_whenPermissionsIsAdminOk() {
        val flux = webClient.get()
            .uri("dsl-abac/$jobjectId?sort=id:desc")
            .header("Authorization", "Bearer $adminToken")
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { it != null }
            .verifyComplete()
    }

    @Test
    fun cFindAll_whenPermissionsIsIpOk() {
        val flux = webClient.get()
            .uri("dsl-abac/$jobjectId?sort=id:desc")
            .header("Authorization", "Bearer $userToken")
            .header("X-Forwarded-For", correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { it != null }
            .verifyComplete()
    }

    @Test
    fun dFindAll_whenPermissionsIsNotOk() {
        val entity = webClient.get()
            .uri("dsl-abac/$jobjectId?sort=id:desc")
            .header("Authorization", "Bearer $userToken")
            .header("X-Forwarded-For", nonCorrectIp)
            .retrieve()
            .toBodilessEntity()

        StepVerifier.create(entity)
            .expectSubscription()
            .verifyError(WebClientResponseException::class.java)
    }

    @Test
    fun eDelete() {
        val deleteResponse = webClient.delete()
            .uri("dsl-abac?id=$jobjectId")
            .header("Authorization", "Bearer $userToken")
            .retrieve()
            .toBodilessEntity()

        StepVerifier.create(deleteResponse)
            .expectSubscription()
            .expectNextMatches { it.statusCodeValue == 200 }
            .verifyComplete()
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