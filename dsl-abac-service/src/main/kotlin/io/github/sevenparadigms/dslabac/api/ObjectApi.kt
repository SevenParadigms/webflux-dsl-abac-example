package io.github.sevenparadigms.dslabac.api

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.dslabac.data.Jfolder
import io.github.sevenparadigms.dslabac.data.Jobject
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@ReactiveFeignClient(name = "dsl-abac-service")
@RequestMapping(path = ["/dsl-abac"])
interface ObjectApi {
    @GetMapping
    fun folders(): Flux<Jfolder>

    @GetMapping(value = ["{jfolderId}"])
    fun findAll(@PathVariable jfolderId: UUID, dsl: Dsl): Flux<Jobject>

    @PostMapping
    fun save(@RequestBody jobject: Jobject): Mono<Jobject>

    @DeleteMapping
    fun delete(dsl: Dsl): Mono<ServerResponse>

    @GetMapping("/listen", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun listener(): Flux<JsonNode>

    @GetMapping("/context")
    fun context(): Flux<Any>
}