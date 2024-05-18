FROM maven:3.8.1-openjdk-17-slim AS build

WORKDIR /app

RUN chmod 777 -R /app

RUN apt update && apt install -y wget

CMD mvn spring-boot:run