# ---- Build stage ----
FROM eclipse-temurin:17-jdk as builder

WORKDIR /app

COPY . .

RUN ./mvnw -q -DskipTests clean package

# ---- Run stage ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
