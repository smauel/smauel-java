# 📦 users-api

A Spring Boot microservice responsible for user management

---

## 🚀 Features

* RESTful API for user registration, authentication, and profile management
* JWT-based authentication
* Maven-based build
* Docker-ready
* Part of a multi-module monorepo

---

## 🧑‍💻 Getting Started

### Prerequisites

* Java 21
* Maven 3.9+
* Docker (for containerized builds)

---

### 🔨 Build the JAR

```bash
mvn clean package
```

The output will be in `target/users-api.jar`.

---

### 🐳 Build and Run with Docker

```bash
# Build the image
mvn clean package -Pdocker

# Run the container
docker run -p 8080:8080 users-api:latest
```

---

## 🧪 Running Tests

### Unit Tests

```bash
mvn test
```

### Acceptance Tests

```bash
mvn verify
```

---

## 🧰 Profiles

This project uses Maven profiles for optional tasks:

|  Profile  |      Description      |
|-----------|-----------------------|
| `docker`  | Builds a Docker image |
| `rewrite` | Trigger OpenRewrite   |

Activate with:

```bash
mvn clean package -P{{profile}}
```

