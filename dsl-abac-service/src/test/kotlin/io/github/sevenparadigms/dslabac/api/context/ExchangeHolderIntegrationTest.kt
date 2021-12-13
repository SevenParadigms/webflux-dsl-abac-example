package io.github.sevenparadigms.dslabac.api.context

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
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
                it.stream().allMatch { value -> value != null } &&
                        it[1] == correctIp && it[3] == adminToken
            }
            .thenCancel()
            .verify()
    }

    @Test
    fun `test Holder race condition by one user`() {
        var lastList: List<*>? = null

        val flux = Flux.range(1, 50)
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .flatMap { rangeCount ->
                webClient.get()
                    .uri("dsl-abac/context")
                    .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
                    .header(Constants.AUTHORIZE_IP, correctIp + rangeCount)
                    .exchangeToMono { it.bodyToMono(List::class.java) }
                    .map { it as List<*> }
                    .doOnNext { lastList = it }
            }

        StepVerifier.create(flux)
            .expectNextCount(49)
            .expectNextMatches { lastList?.get(1) == it[1] }
            .thenCancel()
            .verify()
    }

    @Test
    fun `test Holder race condition by two users`() {
        var firstUserList: List<*>? = null
        var secondUserList: List<*>? = null

        val flux = Flux.range(1, 100)
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .flatMap { rangeCount ->
                if (rangeCount % 2 == 0) {
                    webClient.get()
                        .uri("dsl-abac/context")
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
                        .header(Constants.AUTHORIZE_IP, nonCorrectIp + rangeCount)
                        .exchangeToMono { it.bodyToMono(List::class.java) }
                        .map { it as List<*> }
                        .doOnNext{ secondUserList = it }
                } else {
                    webClient.get()
                        .uri("dsl-abac/context")
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
                        .header(Constants.AUTHORIZE_IP, correctIp + rangeCount)
                        .exchangeToMono { it.bodyToMono(List::class.java) }
                        .doOnNext { firstUserList = it }
                }
            }
            .sequential()
            .collectList()

        StepVerifier.create(flux)
            .expectNextMatches {
                firstUserList?.get(1) == it.reversed().stream().filter { list -> list[3] == userToken }.findFirst()
                    .get()[1] &&
                        secondUserList?.get(1) == it.reversed().stream().filter { list -> list[3] == adminToken }
                    .findFirst().get()[1]
            }
            .thenCancel()
            .verify()
    }
}