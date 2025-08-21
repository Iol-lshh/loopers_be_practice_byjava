plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // feign
    api("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // test
    testFixturesImplementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
}
