# ---------- Build stage ----------
    FROM maven:3.9.6-eclipse-temurin-17 AS build
    WORKDIR /workspace/app
    
    # Cache deps
    COPY pom.xml .
    RUN mvn -q -e -DskipTests dependency:go-offline
    
    # Copy sources and build
    COPY src ./src
    RUN mvn -q -DskipTests package
    
    # Extract layers (Spring Boot layertools)
    RUN java -Djarmode=layertools -jar target/*.jar extract
    
    # ---------- Run stage ----------
    FROM eclipse-temurin:17-jre-jammy
    WORKDIR /app
    EXPOSE 9050
    
    # Copy layers
    COPY --from=build /workspace/app/dependencies/ ./lib/
    COPY --from=build /workspace/app/spring-boot-loader/ ./
    COPY --from=build /workspace/app/snapshot-dependencies/ ./lib/
    COPY --from=build /workspace/app/application/ ./
    
    # Healthcheck (opciono; ako želiš, ostavi)
    # RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
    # HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=5 \
    #   CMD curl -fsS http://localhost:9050/actuator/health || exit 1
    
    # JVM tuning (opciono)
    ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
    
    # Aktiviraj docker profil preko env varijable (možeš promeniti u compose-u)
    ENV SPRING_PROFILES_ACTIVE=docker
    
    # Ispravan main class (paket!)
    ENTRYPOINT ["sh","-c","java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]
    