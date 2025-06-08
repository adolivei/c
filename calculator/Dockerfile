# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -pl calculator -am install -DskipTests

# Package stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/calculator/target/calculator-*.jar app.jar

# Create a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]
