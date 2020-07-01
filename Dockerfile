FROM maven:3-openjdk-11 as build
COPY $PWD /fhirspark
WORKDIR /fhirspark
RUN mvn -DskipTests clean package

FROM openjdk:11-jre-slim
COPY --from=build /fhirspark/target/fhirspark-*-jar-with-dependencies.jar /app.jar
CMD ["java", "-jar", "/app.jar"]