package io.github.sevenparadigms.dslabac.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import java.util.*

data class Jobject(
    val id: UUID? = null,
    val jtree: JsonNode,
    @JsonIgnore
    var jfolderId: UUID? = null
)