package io.github.sevenparadigms.dslabac.data

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("jfolder")
data class Jfolder(
    @Id
    val id: UUID? = null,
    val jtree: JsonNode,

)
