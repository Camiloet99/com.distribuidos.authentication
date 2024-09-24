FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY ./target/authentication-1.0.0.jar /app

EXPOSE 8080

CMD ["java", "-jar", "authentication-1.0.0.jar", "--spring.main.class=com.distribuidos.authentication.Application"]
