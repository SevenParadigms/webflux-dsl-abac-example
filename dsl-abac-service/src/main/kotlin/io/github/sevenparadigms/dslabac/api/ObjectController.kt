package io.github.sevenparadigms.dslabac.api

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.service.ObjectService
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import java.util.*

@RestController
class ObjectController(val objectService: ObjectService) : ObjectApi {
    override fun findAll(@PathVariable jfolderId: UUID, dsl: Dsl) = objectService.findAll(dsl)
    override fun save(@PathVariable jfolderId: UUID, @RequestBody jobject: Jobject) = objectService.save(jfolderId, jobject)
    override fun delete(@RequestParam id: UUID) = objectService.delete(id).then(ServerResponse.ok().build())
    override fun listener(): Flux<JsonNode> = objectService.listener()
}