# Overview
This is a technical exercise in OTA for the notes.

# Pre-requisites
- Java 17
- kotlin
- docker
- gradlew

# Stack and concepts applied
- TDD
- BDD
- kotlin
- spring-boot
- kotest
- structured log with ELK (elasticsearch-logstash-kibana)
- Oauth2 Authorization
- rate limiting mechanism
- implement api versioning
- configuration properties rather than @values
- jacoco for the test coverage

# How To

The local deployment will make use of the dev profile which will use H2 as database while the prod
deployment will use the docker in which it will use the MySQL database.
I put the MySQL/docker as a contingency and also to demonstrate multi-profile configuration.
We will H2 for this demo.

## Run Application
```
./gradlew bootRun
```

## Run unit test and coverage
```
./gradlew test
```

## Build Docker Image
```agsl
./gradlew bootBuildImage --imageName=earl/otanotes
```

# Swagger
go to `http://localhost:8080/swagger-ui/index.html#/`