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
            .map { it as List<List<Any>> }

        StepVerifier.create(mono)
            .expectNextMatches {
                it[0].stream().allMatch{ value -> value != null } &&
                it[0][1] == correctIp && it[0][3] == adminToken
            }
            .thenCancel()
            .verify()
    }
}