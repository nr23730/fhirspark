FROM adoptopenjdk/maven-openjdk11:latest
MAINTAINER Niklas Reimer <niklas@nr205.de>

RUN apt update && apt install git -y

ENV FHIRSPARK_HOME=/fhirspark
RUN git clone -b env_var https://github.com/nr23730/fhirspark.git $FHIRSPARK_HOME

WORKDIR $FHIRSPARK_HOME
RUN mvn package

RUN mkdir /opt/app
RUN mv $FHIRSPARK_HOME/target/fhirspark-*-jar-with-dependencies.jar /opt/app/app.jar
RUN rm -rf $FHIRSPARK_HOME
CMD ["java", "-jar", "/opt/app/app.jar"]