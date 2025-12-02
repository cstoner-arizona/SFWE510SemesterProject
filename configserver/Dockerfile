# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy as build

LABEL maintainer="Christopher Stoner <cstoner@arizona.edu>"

ARG JAR_FILE

COPY ${JAR_FILE} app.jar

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf /app.jar)

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-jammy

VOLUME /tmp

ARG DEPENDENCY=/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","org.skate.configserver.ConfigurationServerApplication"]
