FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy source code and configuration
COPY src /app/src
COPY config /app/config

# Compile the application
RUN mkdir -p out \
    && find src/main/java -name "*.java" > sources.txt \
    && javac -d out -sourcepath src/main/java @sources.txt \
    && rm sources.txt

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy compiled classes and runtime configuration
COPY --from=build /app/out /app/out
COPY config /app/config

# Default command launches the interactive REPL
ENTRYPOINT ["java", "-cp", "/app/out", "com.project.bitpacking.Main"]
CMD []

