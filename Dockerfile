FROM openjdk:23-jdk-slim AS jre-build

RUN "$JAVA_HOME/bin/jlink" \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql.rowset,jdk.jfr,jdk.management,jdk.unsupported \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /jre





FROM --platform=$BUILDPLATFORM openjdk:23-jdk-slim AS app-build

# see .dockerignore
COPY --parents config gradle src build.gradle gradlew gradlew.bat settings.gradle /app/repo/

WORKDIR /app/repo
RUN JAVA_OPTS="-Dhttp.socketTimeout=60000 -Dhttps.socketTimeout=60000" ./gradlew bootJar
RUN cp /app/repo/build/libs/*.jar /app/app.jar






FROM openjdk:23-jdk-slim

RUN useradd -ms /bin/bash app
USER app

ENV JAVA_HOME=/jre
COPY --from=jre-build /jre $JAVA_HOME

WORKDIR /app

COPY --from=app-build /app/app.jar /app/app.jar

CMD ["/jre/bin/java", "-jar", "/app/app.jar"]
