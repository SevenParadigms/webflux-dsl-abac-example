package io.github.sevenparadigms.dslabac.data

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono
import java.util.*

interface FolderRepository: R2dbcRepository<Jfolder, UUID>{
    @Query("select jfolder(:jtreeName)")
    fun findFolderIdByJtreeName(jtreeName: String): Mono<UUID>
}