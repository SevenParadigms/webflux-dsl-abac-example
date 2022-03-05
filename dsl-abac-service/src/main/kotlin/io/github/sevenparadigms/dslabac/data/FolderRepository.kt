package io.github.sevenparadigms.dslabac.data

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono
import java.io.Serializable
import java.util.*

interface FolderRepository: R2dbcRepository<Jfolder, UUID>{
    @Query("select jfolder(:jtreeName)")
    fun findFolderIdByJtreeName(jtreeName: String): Mono<UUID>
}

data class Jfolder(
    val id: UUID? = null,
    val jtree: JsonNode
) : Serializable
