package io.github.sevenparadigms.dslabac.api.context

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import reactor.test.StepVerifier

class ExchangeHolderIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun testHolder() {
        val mono = webClient.get()
            .uri("dsl-abac/context")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .exchangeToMono { it.bodyToMono(List::class.java) }
            .map { it as List<*> }

        StepVerifier.create(mono)
            .expectNextMatches {
                it.stream().allMatch{ value -> value != null } &&
                it[1] == correctIp && it[3] == adminToken
            }
            .thenCancel()
            .verify()
    }
}