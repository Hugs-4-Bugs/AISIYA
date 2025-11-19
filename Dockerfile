# -------- build stage --------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . /workspace
# ensure maven wrapper fallback doesn't matter
RUN mvn -B -f /workspace/pom.xml clean package -DskipTests

# -------- runtime stage --------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# copy the jar produced by the build stage
COPY --from=build /workspace/target/*.jar app.jar

# let Java bind to Render's provided port env var
ENV JAVA_OPTS=""
ENV PORT 10000

# start with server.port set from env
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
