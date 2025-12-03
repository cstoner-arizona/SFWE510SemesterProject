# SFWE 510 Semester Project - Skate Spots

## About

The Skate Spot Service is a microservices-based application designed to help skateboarders discover, document, and share skating locations. 
Built with Spring Boot and following cloud-native architecture patterns, this service allows users to:

- **Discover Spots**: Find skating locations by name, geographic area, or difficulty rating
- **Document Locations**: Create detailed spot profiles including location data, surface quality ratings, difficulty assessments, and photos
- **Community Sharing**: See what spots other skaters have discovered and what tricks they've landed

### Architecture

This project demonstrates a modern microservices architecture with:

- **Spring Cloud Config Server**: Centralized configuration management for all services
- **PostgreSQL Database**: Persistent storage with proper schema management via SQL initialization scripts
- **RESTful API**: Clean, resource-oriented endpoints following REST best practices
- **Docker Compose**: Containerized deployment with service orchestration and health checks
- **Spring Data JPA**: Simplified database interactions with entity relationships and repository pattern

The service is designed to be part of a larger ecosystem of skating-related microservices, with clear separation of concerns and the ability to integrate with future services for user management, social features, and more.

### Tech Stack

- Java 21
- Spring Boot 3.3.4
- Spring Cloud Config 2023.0.3
- PostgreSQL
- Docker & Docker Compose
- Maven
- Hibernate/JPA
- Lombok

## How to Run

Clone the repository and navigate to the project directory:

```bash
git clone https://github.com/cstoner-arizona/SFWE510SemesterProject.git
cd SFWE510SemesterProject
```

Build the JAR files using Maven and create Docker images:

```bash
mvn clean package dockerfile:build
```

And now, we can use Docker Compose to start the application:

```bash
docker-compose -f docker/docker-compose.yml --profile dev up
```

## Postman

Once running, you can utilize the Postman collections to start interacting with each service.
There is 2 collections, one to ping directly to a service, and one to instead use the gateway.
