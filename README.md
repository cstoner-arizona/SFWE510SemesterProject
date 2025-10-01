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

## URLS & Endpoints

Once the application is running, you can access the following urls:

- Configuration Server: `http://localhost:8089`
- Spot Service: `http://localhost:8080`
- Database: `http://localhost:5432` (postgres/postgres)

Then the following endpoints are available to use the spot service:

### Spot CRUD

* POST : /api/spots : Creates a new spot
* GET : /api/spots : Retrieves all spots
* GET : /api/spots/{id} : Retrieves a spot by assigned UUID
* PUT : /api/spots/{id} : Updates a spot by assigned UUID
* DELETE : /api/spots/{id} : Deletes a spot by assigned UUID

### Search

* GET : /api/spots/search?name={name} : Searches spots by name
* GET : /api/spots/founder/{founderSkaterId} : Retrieves spots by founder skater ID
* GET : /api/spots/area?minLat={minLat}&maxLat={maxLat}&minLng={minLng}&maxLng={maxLng} : Searches spots within a geographic area

### Trick Attempts

* POST : /api/spots/{spotId}/trick-attempts : Adds a trick attempt to a spot (requires X-Skater-Id header)
* GET : /api/spots/{spotId}/trick-attempts : Retrieves all trick attempts for a spot