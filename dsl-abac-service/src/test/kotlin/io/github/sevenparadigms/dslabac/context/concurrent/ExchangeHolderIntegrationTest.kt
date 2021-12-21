package io.github.sevenparadigms.dslabac.context.concurrent

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.testing.AbstractIntegrationMultithreadingTest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.time.Duration

class ExchangeHolderIntegrationTest : AbstractIntegrationMultithreadingTest() {

    @Test
    fun testHolder() {
        val mono = webClient.get()
            .uri("dsl-abac/context")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .header(Constants.AUTHORIZE_IP, correctIp)
            .exchangeToMono { it.bodyToMono(List::class.java) }

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
        val flux = Flux.range(1, 50)
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .flatMap { rangeCount ->
                val testIp = correctIp + rangeCount
                webClient.get()
                    .uri("dsl-abac/context")
                    .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
                    .header(Constants.AUTHORIZE_IP, testIp)
                    .exchangeToMono { it.bodyToMono(List::class.java) }
                    .zipWith(Mono.just(testIp))
            }

        StepVerifier.create(flux)
            .expectNextCount(49)
            .expectNextMatches { it.t2 == it.t1[1] }
            .thenCancel()
            .verify()
    }

    @Test
    fun `test Holder race condition by two users`() {
        val flux = Flux.range(1, 100)
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .flatMap { rangeCount ->
                val testIp: String
                if (rangeCount % 2 == 0) {
                    testIp = nonCorrectIp + rangeCount
                    webClient.get()
                        .uri("dsl-abac/context")
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
                        .header(Constants.AUTHORIZE_IP, testIp)
                        .exchangeToMono { it.bodyToMono(List::class.java) }
                        .zipWith(Mono.just(testIp))
                } else {
                    testIp = correctIp + rangeCount
                    webClient.get()
                        .uri("dsl-abac/context")
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
                        .header(Constants.AUTHORIZE_IP, testIp)
                        .exchangeToMono { it.bodyToMono(List::class.java) }
                        .zipWith(Mono.just(testIp))
                }
            }

        StepVerifier.create(flux)
            .expectNextCount(98)
            .expectNextMatches { it.t2 == it.t1[1] }
            .expectNextMatches { it.t2 == it.t1[1] }
            .thenCancel()
            .verify()
    }

    @Test
    fun `test concurrency by countUsersForTest`() {
        val responsesFlux = Flux.range(0, countUsersForTest)
            .delayElements(Duration.ofMillis(20))
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .flatMap { getToken("test$it").zipWith(Mono.just(it)) }
            .flatMap {
                val testIp = correctIp + it.t2
                webClient.get()
                    .uri("dsl-abac/context")
                    .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + it.t1)
                    .header(Constants.AUTHORIZE_IP, testIp)
                    .exchangeToMono { resp -> resp.bodyToMono(List::class.java) }
                    .zipWith(Mono.just(testIp))
            }

        StepVerifier.create(responsesFlux)
            .thenConsumeWhile { it.t2 == it.t1[1] }
            .verifyComplete()
    }
}