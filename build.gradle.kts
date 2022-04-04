plugins {
	 checkstyle
	`java-library`
}

subprojects {
	group = "io.github.sevenparadigms.dsl-abac"
	version = "1.0.0"
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

repositories {
	mavenLocal()
	mavenCentral()
}