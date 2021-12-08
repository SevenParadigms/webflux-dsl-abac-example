package io.github.sevenparadigms.dslabac.dto

import com.fasterxml.jackson.databind.JsonNode
import io.github.sevenparadigms.dslabac.data.Jfolder
import java.util.*

data class JobjectDto(
    val id: UUID? = null,
    val jtree: JsonNode,
    val jfolder: Jfolder? = null
)