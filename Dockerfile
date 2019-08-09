FROM maven:3.6.1-jdk-12 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:12-jre-slim
COPY --from=build /home/app/target/UofU-CS-Bot.jar /usr/local/lib/UofU-CS-Bot.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/UofU-CS-Bot.jar"]
