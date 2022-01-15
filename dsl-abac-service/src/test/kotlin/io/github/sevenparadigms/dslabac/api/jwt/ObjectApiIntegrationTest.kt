package io.github.sevenparadigms.dslabac.api.jwt

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.testing.jwt.AbstractIntegrationTest
import io.github.sevenparadigms.dslabac.data.Jobject
import org.junit.FixMethodOrder
import org.junit.jupiter.api.Test
import org.junit.runners.MethodSorters
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ObjectApiIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun aSave() {
        val jobject = Jobject(
            jfolderId = jfolderId,
            jtree = objectMapper.readTree("{\"name\": \"Testin123g\", \"description\": \"Testing\"}")
        )

        val saveResponse = webClient.post()
            .uri("dsl-abac")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .body(BodyInserters.fromPublisher(Mono.just(jobject), Jobject::class.java))
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
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun cFindAll_whenPermissionsIsIpOk() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun dFindAll_whenPermissionsIsNotOk() {
        val entity = webClient.get()
            .uri("dsl-abac/$jobjectId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
            .header(Constants.AUTHORIZE_IP, nonCorrectIp)
            .retrieve()
            .toBodilessEntity()

        StepVerifier.create(entity)
            .expectSubscription()
            .verifyError(WebClientResponseException::class.java)
    }

    @Test
    fun eDelete() {
        val deleteResponse = webClient.delete()
            .uri("dsl-abac?query=jfolderId==$jobjectId")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
            .retrieve()
            .toBodilessEntity()

        StepVerifier.create(deleteResponse)
            .expectSubscription()
            .expectNextMatches { it.statusCodeValue == 200 }
            .verifyComplete()
    }
}