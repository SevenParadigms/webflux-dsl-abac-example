package io.github.sevenparadigms.dslabac.feature

import org.sevenparadigms.cache.hazelcast.AnySerializable
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.r2dbc.repository.query.Equality
import org.springframework.data.r2dbc.repository.query.ReadOnly
import org.springframework.expression.Expression
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*

interface FeatureRepository: R2dbcRepository<Feature, UUID>

data class Feature(
    var id: UUID? = null,

    // SpEL Expression with database reserved word as column name
    var group: Expression? = null,

    // by name
    var version: Int? = null,

    @Version // r2dbc-commons
    var versionAnn: ZonedDateTime? = null,

    // from application.yml
    var customVersion: Long? = null,

    // by name
    var createdAt: OffsetDateTime? = null,

    @CreatedDate // r2dbc-commons
    var createdAnn: ZonedDateTime? = null,

    // from application.yml
    var customCreate: LocalDateTime? = null,

    // from application.yml
    var customUpdate: OffsetDateTime? = null,

    @LastModifiedDate // r2dbc-commons
    var attrNow: LocalDateTime? = null,

    @Equality // error if try set not equals of previous
    var equality: Int? = null,

    // from application.yml
    var customEquality: String? = null,

    @ReadOnly // always rewrite to previous
    var readonly: String? = null,

    // from application.yml
    var customReadonly: Int? = null,

) : AnySerializable // universal hazelcast serializable