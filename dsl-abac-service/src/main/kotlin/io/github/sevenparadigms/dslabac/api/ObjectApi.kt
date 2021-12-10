package io.github.sevenparadigms.dslabac.api

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.dslabac.data.Jobject
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@ReactiveFeignClient(name = "dsl-abac-service")
@RequestMapping(path = ["/dsl-abac"])
interface ObjectApi {
    @PreAuthorize("hasPermission(#dsl, 'findAll')")
    @GetMapping(value = ["{jfolderId}"])
    fun findAll(@PathVariable jfolderId: UUID, dsl: Dsl): Flux<Jobject>

    @PostMapping(value = ["{jfolderId}"])
    fun save(@PathVariable jfolderId: UUID, @RequestBody jobject: Jobject): Mono<Jobject>

    @DeleteMapping
    fun delete(@RequestParam id: UUID): Mono<ServerResponse>

    @GetMapping("/listen", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun listener(): Flux<JsonNode>

    @GetMapping("/context")
    fun context(): Flux<List<Any>>
}