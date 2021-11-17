package io.github.sevenparadigms.dslabac.data

import org.springframework.data.r2dbc.repository.R2dbcRepository
import java.util.*

interface ObjectRepository: R2dbcRepository<Jobject, UUID>