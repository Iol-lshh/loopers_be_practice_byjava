rootProject.name = "loopers-java-spring-template"

include(
    ":apps:commerce-api",
    ":apps:commerce-collector",
    ":apps:pg-simulator",
    ":modules:jpa",
    ":modules:redis",
    ":modules:feign",
    ":modules:resilience",
    ":modules:kafka",
    ":supports:jackson",
    ":supports:logging",
    ":supports:monitoring",
    ":supports:uuid",
    ":sharing:error",
    ":sharing:event",
)

// configurations
pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
            }
        }
    }
}
