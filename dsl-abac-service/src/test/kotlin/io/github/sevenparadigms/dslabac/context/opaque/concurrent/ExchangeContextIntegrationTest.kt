package io.github.sevenparadigms.dslabac.context.opaque.concurrent

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.testing.opaque.AbstractIntegrationMultithreadingTest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.time.Duration

class ExchangeContextIntegrationTest : AbstractIntegrationMultithreadingTest() {

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
                    .zipWith(Mono.just(it.t2))
            }

        StepVerifier.create(responsesFlux)
            .thenConsumeWhile { exchangeContext.getRemoteIp("test${it.t2}") == it.t1[1] }
            .verifyComplete()
    }

    @Test
    fun `test concurrency rewrite in ExchangeContext by countUsersForTest`() {
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
                    .zipWith(Mono.just(it.t2))
                    .zipWith(Mono.just(it.t1))
            }
            .flatMap {
                val testIp = it.t1.t1[1].toString() + it.t1.t2
                webClient.get()
                    .uri("dsl-abac/context")
                    .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + it.t2)
                    .header(Constants.AUTHORIZE_IP, testIp)
                    .exchangeToMono { resp -> resp.bodyToMono(List::class.java) }
                    .zipWith(Mono.just(it.t1.t2))
            }

        StepVerifier.create(responsesFlux)
            .thenConsumeWhile { exchangeContext.getRemoteIp("test${it.t2}") == it.t1[1] }
            .verifyComplete()
    }

}