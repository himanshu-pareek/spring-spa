plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

subprojects {
	afterEvaluate {
		if (tasks.findByName("buildWebapp") != null) {
			val webAppName = project.name
			rootProject.tasks.processResources {
				from(tasks.named("buildWebapp")) {
					into("templates/$webAppName")
				}
			}
		}
	}
}

/*
app1 -> task (buildWebApp) -> app1/build/webapp
app2 -> task (buildWebApp) -> app2/dist

app1/build/webapp -> build/resources/main/templates/app1
app2/dist -> build/resources/main/templates/app2
 */
