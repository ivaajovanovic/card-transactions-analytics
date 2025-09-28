# ---------- Build stage ----------
    FROM maven:3.9.9-eclipse-temurin-17 AS build
    WORKDIR /workspace/app
    
    # Keširaj dependency-je
    COPY pom.xml .
    RUN mvn -q -DskipTests dependency:go-offline
    
    # Kopiraj izvorni kod i napravi fat JAR
    COPY src ./src
    RUN mvn -q -DskipTests clean package
    
    # ---------- Runtime stage ----------
    FROM eclipse-temurin:17-jre-jammy
    WORKDIR /app
    
    # Kopiraj Spring Boot "fat" JAR koji smo spakovali sa classifier-om 'exec'
    # (npr. target/ColumnarDatabaseService-0.0.1-SNAPSHOT-exec.jar)
    COPY --from=build /workspace/app/target/*-exec.jar /app/app.jar
    
    # Port tvog servisa (iz application.properties: server.port=9050)
    EXPOSE 9050
    
    # (opciono) healthcheck – možeš i da ukloniš ako ne treba
    HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
      CMD wget -qO- http://localhost:9050/actuator/health | grep '"status":"UP"' || exit 1
    
    # Pokretanje
    ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
    