dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal {
            mavenContent {
                releasesOnly()
            }
        }
        mavenCentral {
            mavenContent {
                releasesOnly()
            }
        }
    }
}
rootProject.name = "dsl-abac-service"
enableFeaturePreview("VERSION_CATALOGS")

include("dsl-abac-service")