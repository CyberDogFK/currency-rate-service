FROM openjdk:21-jdk as chef
LABEL authors="antonpavliuk"
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 5005