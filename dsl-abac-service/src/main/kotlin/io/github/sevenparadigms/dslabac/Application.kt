package io.github.sevenparadigms.dslabac

import io.github.sevenparadigms.abac.configuration.EnableAbacSecurity
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableAbacSecurity
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}