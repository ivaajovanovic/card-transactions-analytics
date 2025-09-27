# syntax=docker/dockerfile:1.6

# 1) BUILD STAGE – noviji Maven + bolje keširanje i retri za mrežu
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace/app

# prvo POM da bi se keširale zavisnosti
COPY pom.xml ./
RUN --mount=type=cache,id=m2-cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline

# sada i ostatak koda
COPY src ./src

# build sa defensivnim HTTP opcijama (fix za "Premature end of Content-Length")
RUN --mount=type=cache,id=m2-cache,target=/root/.m2 \
    mvn -B -DskipTests \
       -Dhttp.keepAlive=false \
       -Dmaven.wagon.http.pool=false \
       -Dmaven.wagon.http.retryHandler.count=3 \
       clean package

# 2) RUNTIME STAGE – mali JRE image, samo pokreni jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar

# (opciono) u tvom compose već mapiraš 9001
EXPOSE 9001

ENTRYPOINT ["java","-jar","/app/app.jar"]
