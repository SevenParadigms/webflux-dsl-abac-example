package io.github.sevenparadigms.dslabac.data

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

data class Jobject(
    val id: UUID? = null,
    val jtree: JsonNode,
    var jfolderId: UUID? = null
)