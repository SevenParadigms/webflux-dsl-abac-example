package io.github.sevenparadigms.dslabac.testing.config

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer
import java.io.File

open class PostgresTestContainer {

    companion object {
        private val dslAbacContainer = createContainer("dsl_abac", "master/initial-script.sql")
        private val abacRulesContainer = createContainer("abac_rules", "security/initial-script.sql")

//        val dslR2DBCContainer = PostgreSQLR2DBCDatabaseContainer(dslAbacContainer)
//        val abacR2DBCContainer = PostgreSQLR2DBCDatabaseContainer(abacRulesContainer)

        fun initDatabase() {
            dslAbacContainer.start()
            abacRulesContainer.start()
        }

        fun stopDatabase() {
            dslAbacContainer.stop()
            abacRulesContainer.stop()
        }

        private fun createContainer(databaseName: String, initScriptPath: String): PostgreSQLContainer<*> {
            val container = PostgreSQLContainer("postgres:13.3")
                .withExposedPorts(5432)
                .withExtraHost("localhost", "127.0.0.1")
                .withInitScript(initScriptPath)
                .withDatabaseName(databaseName)
                .withUsername("postgres")
                .withPassword("postgres")
            container.addFileSystemBind(
                initScriptPath + File.separator + "create.sh",
                "/docker-entrypoint-initdb.d/00_create.sh",
                BindMode.READ_ONLY
            )
            return container
        }
    }

    @BeforeAll
    fun initDatabase() {
        dslAbacContainer.start()
        abacRulesContainer.start()
    }

    @AfterAll
    fun stopDatabase() {
        dslAbacContainer.stop()
        abacRulesContainer.stop()
    }
}