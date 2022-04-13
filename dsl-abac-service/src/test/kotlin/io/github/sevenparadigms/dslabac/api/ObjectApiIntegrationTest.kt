package io.github.sevenparadigms.dslabac.api

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import io.github.sevenparadigms.dslabac.data.Jobject
import org.junit.jupiter.api.Test
import org.sevenparadigms.kotlin.common.objectToJson
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

class ObjectApiIntegrationTest : AbstractIntegrationTest() {

    var jobjectId: UUID? = null

    @Test
    fun `should save and check automatic set values`() {
        val jobject = Jobject(
            jfolderId = jfolderId,
            jtree = "{\"name\": \"Testin123g\", \"description\": \"Testing\"}".objectToJson()
        )

        val saveResponse = webClient.post()
            .uri("dsl-abac")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .body(BodyInserters.fromPublisher(Mono.just(jobject), Jobject::class.java))
            .retrieve()
            .bodyToMono(Jobject::class.java)
            .doOnNext { jobjectId = it.id }

        StepVerifier.create(saveResponse)
            .expectNextMatches { it != null }
            .verifyComplete()
    }

    @Test
    fun `should when permissions is admin`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `should when permissions is ip and some user role`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `should when permissions is not ok`() {
        val entity = webClient.get()
            .uri("dsl-abac/$jobjectId?sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
            .header(Constants.AUTHORIZE_IP, nonCorrectIp)
            .retrieve()
            .toBodilessEntity()

        StepVerifier.create(entity)
            .verifyError(WebClientResponseException::class.java)
    }
}