FROM openjdk:8
WORKDIR /
COPY scripts/wait-for-it.sh   /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

COPY target/rs-api.jar  /app.jar
