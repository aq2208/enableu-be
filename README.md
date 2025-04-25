# EnableU - Inclusive Online Learning Platform

EnableU is an inclusive online learning platform tailored to support neurodivergent individuals, inspired by platforms like Coursera and Udemy. Developed as a capstone project by students at FPT University in collaboration with **Imago Work** — a real-world client advocating for neurodivergent communities — EnableU aims to make digital education accessible, personalized, and empowering.

---

## 🌟 Key Features

- 🎓 **Course Management**: Courses are designed based on Imago Work’s specialized curriculum for neurodivergent learners.
- 👥 **User Roles**: Supports multiple user types — learners, instructors, and admins — with role-based access control.
- 🧠 **Accessibility Focused**: Integrates AI-powered text-to-speech and speech-to-text tools to support diverse learning needs.
- 📱 **Mobile-Friendly**: Built for responsive use on both web and mobile devices.
- 🔐 **Secure Authentication**: Uses **JWT tokens**, phone-based verification, and Spring Security for robust authentication and authorization.
- 📦 **File Storage**: Integrates with **Cloudflare R2 (S3-compatible)** storage for managing course materials and user-uploaded content.

---

## ⚙️ Tech Stack

| Layer              | Technology                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| **Backend**        | `Spring Boot`, `GraphQL`, `Spring Security`, `JWT`, `Lombok`, `Maven`       |
| **Frontend**       | React.js (optional based on actual implementation)                          |
| **Database**       | `MySQL`                                                                     |
| **Object Storage** | `Cloudflare R2` (S3-compatible API)                                         |
| **Authentication** | Phone-based verification, `JWT`, Spring Security                            |
| **DevOps**         | Dockerized environment (optional based on deployment setup)                 |
| **AI Integration** | Text-to-speech & speech-to-text (using external AI services, e.g., Google)  |

---

## 🧩 Architecture

- **Monolithic application** powered by **Spring Boot**.
- **GraphQL** enables flexible data fetching for frontend components.
- **Cloudflare R2** stores course videos, documents, and media files.
- **MySQL** serves as the main data storage for users, courses, and progress tracking.
- Optional use of **Redis** or in-memory caching (if applicable for scaling).

---

## 🛠️ Key Modules

### 1. User & Auth
- Registration and login via phone
- JWT-based session management
- Role-based authorization

### 2. Course & Curriculum
- Instructor dashboard to create and manage lessons
- Learner dashboard to track progress and revisit content
- Support for video, PDF, and interactive content

### 3. Accessibility Enhancements
- TTS and STT powered by AI APIs
- UI components designed for simplicity and clarity
- Audio and visual cue enhancements

---

## 🧪 Testing

- Backend unit and integration tests using `JUnit`
- Security and authorization tests for role-based access
- Manual and user testing with feedback from Imago Work representatives

---

## 👨‍👩‍👧‍👦 Real-World Collaboration

- Built in partnership with [Imago Work](https://imagowork.org/)
- Developed as a capstone project at **FPT University**
- Direct user testing and feedback loop with neurodivergent learners

---

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
