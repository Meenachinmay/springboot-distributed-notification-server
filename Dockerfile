FROM openjdk:17-slim

WORKDIR /app

# Copy specifically the non-plain jar file
COPY build/libs/redis-distributed-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081 8082

ENTRYPOINT ["java", "-jar", "app.jar"]