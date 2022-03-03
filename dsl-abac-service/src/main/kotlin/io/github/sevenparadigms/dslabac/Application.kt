package io.github.sevenparadigms.dslabac

import io.github.sevenparadigms.abac.configuration.EnableAbacSecurity
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@EnableCaching
@EnableAbacSecurity
@EnableR2dbcRepositories
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}