package io.github.sevenparadigms.dslabac.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.abac.security.context.ExchangeHolder
import io.github.sevenparadigms.dslabac.data.Jfolder
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.data.ObjectRepository
import org.sevenparadigms.kotlin.common.objectToJson
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ObjectService(
    private val objectRepository: ObjectRepository
) {
    @Transactional(readOnly = true)
    fun folders(): Flux<Jfolder> = objectRepository.findAllFolder()

    @Transactional(readOnly = true)
    fun findAll(jfolderId: UUID, dsl: Dsl): Flux<Jobject> =
        objectRepository.findAll(dsl.equals("jfolderId", jfolderId))

    @Transactional
    fun save(jobject: Jobject) = objectRepository.save(jobject)

    @Transactional
    fun delete(dsl: Dsl) = objectRepository.delete(dsl)

    fun listener(): Flux<JsonNode> = objectRepository.listener().map { it.parameter!!.objectToJson() }

    fun context(): Flux<Any> {
        return Flux.zip(
            ExchangeHolder.getHeaders(),
            ExchangeHolder.getRemoteIp(),
            ExchangeHolder.getRequest(),
            ExchangeHolder.getToken(),
            ExchangeHolder.getSession(),
            ExchangeHolder.getResponse(),
            ExchangeHolder.getUser()
        )
            .flatMap { Flux.fromIterable(listOf(it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7)) }
    }

    fun current(): Mono<UserPrincipal> {
        return ExchangeHolder.getUserPrincipal()
    }
}