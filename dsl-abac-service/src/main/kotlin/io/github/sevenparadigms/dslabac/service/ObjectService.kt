package io.github.sevenparadigms.dslabac.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.abac.security.context.ExchangeHolder
import io.github.sevenparadigms.dslabac.data.Jfolder
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.data.ObjectRepository
import org.sevenparadigms.kotlin.common.objectToJson
import io.github.sevenparadigms.abac.security.auth.data.toPrincipal
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.data.r2dbc.support.SqlField
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.security.Principal
import java.util.*

@Service
class ObjectService(
    private val objectRepository: ObjectRepository
) {
    @Transactional(readOnly = true)
    fun folders(): Flux<Jfolder> = objectRepository.findAllFolder()

    @Transactional(readOnly = true)
    fun findAll(jfolderId: UUID, dsl: Dsl): Flux<Jobject> =
        objectRepository.findAll(dsl.equals(SqlField.jfolderId, jfolderId))

    @Transactional
    fun save(jobject: Jobject) = objectRepository.save(jobject)

    @Transactional
    fun delete(dsl: Dsl) = objectRepository.delete(dsl)

    fun listener(): Flux<JsonNode> = objectRepository.listener().map { it.payload.objectToJson() }

    fun currentUser(): Mono<UserPrincipal> {
        return ExchangeHolder.getUserPrincipal()
    }

    fun currentToken(token: UsernamePasswordAuthenticationToken): Mono<UserPrincipal> = (token.principal as User).toPrincipal().toMono()

    fun currentPrincipal(principal: Principal): Mono<UserPrincipal> = currentToken(principal as UsernamePasswordAuthenticationToken)

    fun currentAuthentication(authentication: Authentication): Mono<UserPrincipal> = authentication.toPrincipal().toMono()
}