plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // feign
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    api("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // test-fixtures
    testFixturesImplementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    testFixturesImplementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
}
