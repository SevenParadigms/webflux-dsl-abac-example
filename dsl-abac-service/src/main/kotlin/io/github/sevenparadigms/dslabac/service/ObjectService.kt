package io.github.sevenparadigms.dslabac.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.data.ObjectRepository
import org.sevenparadigms.kotlin.common.toJsonNode
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ObjectService(private val objectRepository: ObjectRepository) {
    @Transactional(readOnly = true)
    fun findAll(dsl: Dsl): Flux<Jobject> = objectRepository.findAll(dsl)

    @Transactional
    fun save(jobject: Jobject): Mono<Jobject> = objectRepository.save(jobject)

    @Transactional
    fun delete(id: UUID): Mono<Void> = objectRepository.deleteById(id)

    fun listener(): Flux<JsonNode> = objectRepository.listener().map { it.parameter!!.toJsonNode() }
}