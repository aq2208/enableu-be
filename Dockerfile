# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR .

# Install necessary tools
RUN apt-get update && apt-get install -y curl unzip git

# Install Gradle 8.8
ARG GRADLE_VERSION=8.8
RUN curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip \
    && unzip gradle.zip \
    && rm gradle.zip \
    && mv gradle-${GRADLE_VERSION} /opt/gradle \
    && ln -s /opt/gradle/bin/gradle /usr/bin/gradle

# Copy the Gradle build files
COPY build.gradle settings.gradle ./

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy the project source
COPY src src

# Build the application
RUN gradle build -x test --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR .

# Copy the built artifact from the build stage
COPY --from=build ./build/libs/enableu-0.0.1-SNAPSHOT.jar app.jar

# Set the entrypoint to use the config file from the current directory
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:./application.yml"]

# Expose the port the app runs on
EXPOSE 8080