FROM gradle:8.12-alpine AS temp_build_image
ENV APP_HOME=/usr/app/
COPY . $APP_HOME
WORKDIR $APP_HOME
RUN gradle build

FROM alpine/java:22
ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8081
COPY --from=temp_build_image $APP_HOME/build/libs/kafka*.jar /usr/app/app.jar

ENTRYPOINT ["java","-jar","app.jar"]