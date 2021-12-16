package io.github.sevenparadigms.dslabac.dsl

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import io.github.sevenparadigms.dslabac.data.Jobject
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import reactor.test.StepVerifier

class DslIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun `findAll test case sorting`() {
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
    fun `findAll test case not null`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=!@jtree&sort=id:desc")
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
    fun `findAll test case not null check abac rule`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=!@jtree&fields=id&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
            .retrieve()
            .toBodilessEntity()

        StepVerifier.create(flux)
            .expectError()
            .verify()
    }

    @Test
    fun `findAll test case equals`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jfolder_id==$jfolderId&sort=id:desc")
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
    fun `findAll test case equals jsonb`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jtree.name==Acme&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectSubscription()
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }


}