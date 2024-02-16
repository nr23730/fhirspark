FROM maven:3-eclipse-temurin-21 as build
COPY $PWD /fhirspark
WORKDIR /fhirspark
RUN mvn -DskipTests clean package

FROM gcr.io/distroless/java21-debian12
COPY --from=build /fhirspark/target/fhirspark-*-jar-with-dependencies.jar /app/fhirspark.jar
CMD ["/app/fhirspark.jar"]
