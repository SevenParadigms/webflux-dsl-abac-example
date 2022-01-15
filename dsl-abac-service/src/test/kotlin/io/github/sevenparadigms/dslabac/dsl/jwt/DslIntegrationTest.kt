package io.github.sevenparadigms.dslabac.dsl.jwt

import io.github.sevenparadigms.abac.Constants
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.testing.jwt.AbstractIntegrationTest
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.r2dbc.core.awaitOne
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
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
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case is not null`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=!@jtree&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case is null`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=@jtree&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextCount(0)
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case not null check abac rule`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=!@jtree&fields=id,jtree&sort=id:desc")
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
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case fields with escape`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jtree.name^^Acme&fields=id, jtree&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case jsonb in`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jtree.name^^Acme&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case jsonb not in`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jtree.name^Acme doc&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case jsonb not equals`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jtree.name!=Acme doc&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case jsonb equals like`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jtree.name~~Acme&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case jsonb not equals full search`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=tsv@@Acme&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextCount(2)
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case jsonb full search with equals`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=tsv@@Acme,jtree.name==Acme type&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextCount(1)
            .thenCancel()
            .verify()
    }

    @Test
    fun `findAll test case jsonb join by folderId`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jfolder.id==$jfolderId&fields=jfolder.id, jfolder.jtree&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextMatches { it != null }
            .thenCancel()
            .verify()
    }

    @Test
    fun `delete equals jobject by jsonb`() {
        val jobject = Jobject(
            jfolderId = jfolderId,
            jtree = objectMapper.readTree("{\"name\": \"deleteTesting1\", \"description\": \"Testing\"}")
        )
        runBlocking {
            webClient.post()
                .uri("dsl-abac")
                .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
                .body(BodyInserters.fromPublisher(Mono.just(jobject), Jobject::class.java))
                .retrieve()
                .bodyToMono(Jobject::class.java)
                .doOnNext { jobjectId = it.id }.awaitFirst()
            var exists =
                postgresDatabaseClient.sql("select exists (select 1 from jobject where jtree->>'name' ='deleteTesting1');")
                    .fetch().awaitOne()["exists"] as Boolean

            Assertions.assertTrue(exists)

            webClient.delete()
                .uri("dsl-abac?query=jtree.name==deleteTesting1")
                .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
                .retrieve()
                .toBodilessEntity().awaitFirst()
            exists =
                postgresDatabaseClient.sql("select exists (select 1 from jobject where jtree->>'name' ='deleteTesting1');")
                    .fetch().awaitOne()["exists"] as Boolean

            Assertions.assertFalse(exists)
        }
    }

    @Test
    fun `delete in jobject by jsonb`() {
        val jobject = Jobject(
            jfolderId = jfolderId,
            jtree = objectMapper.readTree("{\"name\": \"deleteTesting1\", \"description\": \"Testing\"}")
        )
        runBlocking {
            webClient.post()
                .uri("dsl-abac")
                .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
                .body(BodyInserters.fromPublisher(Mono.just(jobject), Jobject::class.java))
                .retrieve()
                .bodyToMono(Jobject::class.java)
                .doOnNext { jobjectId = it.id }.awaitFirst()
            var exists =
                postgresDatabaseClient.sql("select exists (select 1 from jobject where jtree->>'name' ='deleteTesting1');")
                    .fetch().awaitOne()["exists"] as Boolean

            Assertions.assertTrue(exists)

            webClient.delete()
                .uri("dsl-abac?query=jtree.name^^deleteTesting1 delete")
                .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + userToken)
                .retrieve()
                .toBodilessEntity().awaitFirst()
            exists =
                postgresDatabaseClient.sql("select exists (select 1 from jobject where jtree->>'name' ='deleteTesting1');")
                    .fetch().awaitOne()["exists"] as Boolean

            Assertions.assertFalse(exists)
        }
    }

    @Test
    fun `equals or jsonb`() {
        val flux = webClient.get()
            .uri("dsl-abac/$jfolderId?query=jtree.name==Acme,()jtree.name==Acme or&sort=id:desc")
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + adminToken)
            .retrieve()
            .bodyToFlux(Jobject::class.java)

        StepVerifier.create(flux)
            .expectNextCount(2)
            .verifyComplete()
    }
}