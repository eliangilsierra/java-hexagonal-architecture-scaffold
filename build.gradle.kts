plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.eliangilsierra"
version = "0.0.1-SNAPSHOT"
description = "Reference scaffold: hexagonal (ports and adapters) architecture in Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val mapstructVersion = "1.5.5.Final"
val testcontainersVersion = "1.20.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	runtimeOnly("com.h2database:h2")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	implementation("org.mapstruct:mapstruct:$mapstructVersion")
	annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.postgresql:postgresql")
	testImplementation("org.awaitility:awaitility")
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Only the Spring Boot fat jar is useful here — without this, `gradle build`
// also produces a plain (dependency-less) jar in build/libs, which turns the
// Dockerfile's `*.jar` copy ambiguous.
tasks.named<Jar>("jar") {
	enabled = false
}
