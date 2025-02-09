FROM alpine/java:22

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8081

ARG JAR_FILE=build/libs/kafka*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]