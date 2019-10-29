# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/dr-squid-service-1.0.jar

# Add the application's jar to the container
ADD ${JAR_FILE} dr-squid-service.jar

COPY target/dependency-jars /run/dependency-jars
ADD target/dr-squid-service-1.0.jar /run/dr-squid-service.jar

# Run the jar file
ENTRYPOINT java -jar run/dr-squid-service.jar -D exec.mainClass="com.expediagroup.service.drsquidservice.Application"