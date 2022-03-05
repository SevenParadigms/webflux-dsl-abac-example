package io.github.sevenparadigms.dslabac

import io.github.sevenparadigms.abac.configuration.EnableAbacSecurity
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import java.time.ZoneOffset
import java.util.*

@EnableAbacSecurity
@EnableR2dbcRepositories
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))
    runApplication<Application>(*args)
}