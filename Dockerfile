FROM maven:3.9.6-amazoncorretto-21-debian as build

COPY src /app/src
COPY pom.xml /app

WORKDIR /app
RUN mvn clean install

FROM amazoncorretto:21

ENV DB_URL=jdbc:postgresql://db-eventostec.cxaqsm4iiyjr.us-east-1.rds.amazonaws.com/postgres
ENV DB_USER=postgres
ENV DB_PASSWORD={DB_PASSWORD}
ENV ADMIN_KEY={ADMIN_KEY}
ENV AWS_REGION=us-east-1
ENV AWS_BUCKET_NAME=eventostec-imagens

COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

EXPOSE 80

CMD ["java", "-jar", "app.jar"]