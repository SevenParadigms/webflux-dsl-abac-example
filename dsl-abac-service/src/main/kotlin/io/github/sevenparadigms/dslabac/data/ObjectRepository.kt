package io.github.sevenparadigms.dslabac.data

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import java.util.*

interface ObjectRepository: R2dbcRepository<Jobject, UUID> {
    @Query("select distinct jtree(null)")
    fun findAllFolder(): Flux<Jfolder>
}