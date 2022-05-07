package io.github.sevenparadigms.dslabac.data

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import java.io.Serializable
import java.time.OffsetDateTime
import java.util.*

interface ObjectRepository: R2dbcRepository<Jobject, UUID> {
    @Query("select distinct jtree(null)")
    fun findAllFolder(): Flux<Jfolder>
}

data class Jobject(
    var id: UUID? = null,
    var jtree: JsonNode,
    var jfolderId: UUID? = null,
    var createdAt: OffsetDateTime? = null,
    var createdBy: UUID? = null,
    var updatedAt: OffsetDateTime? = null,
    var updatedBy: UUID? = null
) : Serializable