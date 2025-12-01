# Stage 1: Build the application
FROM openjdk:21-slim as build

LABEL maintainer="Christopher Stoner <cstoner@arizona.edu>"

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]