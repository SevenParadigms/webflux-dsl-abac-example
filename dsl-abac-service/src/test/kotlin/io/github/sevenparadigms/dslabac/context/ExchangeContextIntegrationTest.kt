package io.github.sevenparadigms.dslabac.context

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import io.github.sevenparadigms.dslabac.data.Jobject
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import reactor.test.StepVerifier

class ExchangeContextIntegrationTest : AbstractIntegrationTest() {

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
}