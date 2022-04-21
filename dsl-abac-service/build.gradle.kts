import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.6"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("org.liquibase.gradle") version "2.1.1"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

liquibase {
	activities.register("security") {
		this.arguments = mapOf(
			"logLevel" to "warn",
			"changeLogFile" to "src/main/resources/security.yaml",
			"url" to project.extra.properties["security_url"],
			"username" to project.extra.properties["security_username"],
			"password" to project.extra.properties["security_password"]
		)
	}
	activities.register("master") {
		this.arguments = mapOf(
			"logLevel" to "warn",
			"changeLogFile" to "src/main/resources/master.yaml",
			"url" to project.extra.properties["master_url"],
			"username" to project.extra.properties["master_username"],
			"password" to project.extra.properties["master_password"]
		)
	}

	runList = "security,master"
}

dependencies {
	implementation(libs.bundles.kotlin)
	implementation(libs.bundles.service)
	implementation(libs.bundles.feign)
	implementation(variantOf(libs.transport) { classifier("linux-x86_64") })
	liquibaseRuntime(libs.bundles.liquibase)
	testImplementation(libs.bundles.test)
}
