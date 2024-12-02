# Base image
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy the jar file
COPY target/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar app/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar

# Openning the protocol port
EXPOSE 6433

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app/DAI_Practical2_Connect4-1.0-SNAPSHOT.jar"]

# Set the default command
CMD ["--help"]