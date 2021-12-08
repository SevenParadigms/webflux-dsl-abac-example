package io.github.sevenparadigms.dslabac.service

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.dslabac.data.FolderRepository
import io.github.sevenparadigms.dslabac.data.Jobject
import io.github.sevenparadigms.dslabac.data.ObjectRepository
import io.github.sevenparadigms.dslabac.dto.JobjectDto
import org.sevenparadigms.kotlin.common.toJsonNode
import org.springframework.data.r2dbc.repository.query.Dsl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ObjectService(
    private val objectRepository: ObjectRepository,
    private val folderRepository: FolderRepository
) {
    @Transactional(readOnly = true)
    fun findAll(dsl: Dsl): Flux<JobjectDto> {
        return objectRepository.findAll(dsl)
            .zipWith(
                folderRepository.findAll().collectMap({ it.id }, { it })
            )
            .map { JobjectDto(it.t1.id, it.t1.jtree, it.t2[it.t1.jfolderId]) }
    }

    @Transactional
    fun save(jobject: JobjectDto): Mono<Jobject> =
        folderRepository.findFolderIdByJtreeName(jobject.jfolder!!.jtree.get("name").textValue())
            .flatMap { jfolderId -> objectRepository.save(Jobject(jtree = jobject.jtree, jfolderId = jfolderId)) }

    @Transactional
    fun delete(id: UUID): Mono<Void> = objectRepository.deleteById(id)

    fun listener(): Flux<JsonNode> = objectRepository.listener().map { it.parameter!!.toJsonNode() }
}