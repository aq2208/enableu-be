# enableu - be

## Developer Instruction

### Technologies
- [JDK 17 (Amazon Corretto)](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
- Spring Boot 3.4.4
- Gradle 7.3.3
- Docker

### Run the application

#### Step 1: Navigate to the project directory
```
cd enableu-be
```

#### Step 2: Start database with Docker
```
sudo docker compose up -d db
```

#### Step 3: Build the application
```
./gradlew clean build
```

#### Step 4: Run the application
```
./gradlew bootRun
```

Default port is 8080. Access the application at http://localhost:8080
