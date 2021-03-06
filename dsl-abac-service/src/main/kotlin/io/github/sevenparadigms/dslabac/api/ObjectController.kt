package io.github.sevenparadigms.dslabac.api

import io.github.sevenparadigms.abac.security.auth.data.UserPrincipal
import io.github.sevenparadigms.dslabac.data.Jfolder
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.service.ObjectService
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import java.security.Principal
import java.util.*

@RestController
class ObjectController(val objectService: ObjectService) : ObjectApi {
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun folders(): Flux<Jfolder> = objectService.folders()
    @PreAuthorize("hasPermission(#dsl, 'findAll')")
    override fun findAll(@PathVariable jfolderId: UUID, dsl: Dsl) = objectService.findAll(jfolderId, dsl)
    override fun save(@RequestBody jobject: Jobject) = objectService.save(jobject)
    @PreAuthorize("hasRole('ROLE_USER')")
    override fun delete(dsl: Dsl) = objectService.delete(dsl).then(ServerResponse.ok().build())
    override fun listener() = objectService.listener()
    override fun currentUser() = objectService.currentUser()
    override fun currentToken(token: UsernamePasswordAuthenticationToken) = objectService.currentToken(token)
    override fun currentPrincipal(principal: Principal) = objectService.currentPrincipal(principal)
    override fun currentAuthentication(authentication: Authentication) = objectService.currentAuthentication(authentication)
}