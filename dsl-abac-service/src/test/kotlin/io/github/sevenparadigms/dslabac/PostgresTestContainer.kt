package io.github.sevenparadigms.dslabac

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import io.r2dbc.spi.ConnectionFactories
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Value
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class PostgresTestContainer {

    internal class KContainer(image: DockerImageName) : PostgreSQLContainer<KContainer>(image)

    internal lateinit var postgresDatabaseClient: DatabaseClient

    @Value("\${spring.security.abac.url}")
    private lateinit var databaseUrl: String

    @BeforeAll
    internal fun initApp() {
        postgresDatabaseClient = DatabaseClient.create(ConnectionFactories.get(databaseUrl))
    }

    companion object {
        @JvmStatic
        @Container
        private val postgresContainer = PostgreSQLR2DBCDatabaseContainer(
            KContainer(DockerImageName.parse("jordemort/postgres-rum:latest")
                .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("test-db")
                .withUsername("postgres")
                .withPassword("postgres")
                .withCreateContainerCmdModifier { cmd ->
                    cmd.withHostConfig(
                        HostConfig().withPortBindings(PortBinding(Ports.Binding.bindPort(5555), ExposedPort(5432)))
                    )
                }
                .withReuse(true)
                .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/"
                )
        )
    }
}