package io.github.sevenparadigms.dslabac.api.context

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.dslabac.AbstractIntegrationMultithreadingTest
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier

class ExchangeHolderIntegrationTest : AbstractIntegrationMultithreadingTest() {

    private val countUsersForTest = 1000

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
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .flatMap {
                webClient.post()
                    .uri("auth")
                    .body(BodyInserters.fromPublisher(Mono.just(createUser("test$it")), UserPrincipal::class.java))
                    .exchangeToMono { resp -> resp.bodyToMono(String::class.java) }
                    .zipWith(Mono.just(it))
            }
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

    @BeforeAll
    fun beforeTests() {
        runBlocking {
            val userQueryBuffer = StringBuffer("insert into local_user(login, password) values")
            val authorityQueryBuffer = StringBuffer("insert into authority_user(authority_id, user_id) values")

            Flux.range(0, countUsersForTest)
                .parallel(10)
                .runOn(Schedulers.boundedElastic())
                .flatMap {
                    userQueryBuffer.append("('test$it', 'b9y3FIltPBbk7rrP80Tav8CTHRBRfg=='),")
                    authorityQueryBuffer.append("(getAuthority('ROLE_USER'), getUser('test$it')),")
                    Flux.just(it)
                }
                .doOnComplete {
                    runBlocking {
                        databaseClient.sql(userQueryBuffer.substring(0, userQueryBuffer.length - 1) + ";").fetch()
                            .rowsUpdated().awaitLast()
                    }
                    databaseClient.sql(authorityQueryBuffer.substring(0, authorityQueryBuffer.length - 1) + ";").fetch()
                        .rowsUpdated().subscribe()
                }
                .awaitLast()
        }
    }

    @AfterAll
    fun afterTests() {
        databaseClient.sql("delete from local_user where login like 'test%';").fetch().rowsUpdated()
            .subscribe()
    }
}