package io.github.sevenparadigms.dslabac.context

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
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
            .expectNextMatches { exchangeContext.getResponse("admin") != null }
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
            .expectNextMatches { exchangeContext.getUser("admin") != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun getCurrentUser() {
        val userPrincipal = webClient.get()
            .uri("/dsl-abac/current-user")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToMono(UserPrincipal::class.java)

        StepVerifier.create(userPrincipal)
            .expectNextMatches { it.login == "admin" && it.id != null}
            .thenCancel()
            .verify()
    }

    @Test
    fun getCurrentToken() {
        val userPrincipal = webClient.get()
            .uri("/dsl-abac/current-token")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToMono(UserPrincipal::class.java)

        StepVerifier.create(userPrincipal)
            .expectNextMatches { it.login == "admin" }
            .thenCancel()
            .verify()
    }

    @Test
    fun getCurrentPrincipal() {
        val userPrincipal = webClient.get()
            .uri("/dsl-abac/current-principal")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToMono(UserPrincipal::class.java)

        StepVerifier.create(userPrincipal)
            .expectNextMatches { it.login == "admin" }
            .thenCancel()
            .verify()
    }

    @Test
    fun currentAuthentication() {
        val userPrincipal = webClient.get()
            .uri("/dsl-abac/current-authentication")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToMono(UserPrincipal::class.java)

        StepVerifier.create(userPrincipal)
            .expectNextMatches { it.login == "admin" && it.id != null }
            .thenCancel()
            .verify()
    }
}