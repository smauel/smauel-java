# 📦 smauel Monorepo

Monorepo for the smauel platform

---

## 🚀 Features

* TODO

---

## 🧑‍💻 Getting Started

### Prerequisites

* Java 21
* Maven 3.9+
* Docker (for containerized builds)

---

### 🔨 Build

```bash
mvn clean package
```

---

### 🐳 Build and Run with Docker

```bash
# Build the image
mvn clean package -Pdocker

# Run the platform
TODO: docker-compose/kubernetes
```

---

## 🧪 Running Tests

### Unit Tests

```bash
mvn test
```

### Acceptance Tests

```bash
mvn test
```

### Integration Tests

```bash
TODO: bruno
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

---

## 📂 Project Structure

├─ backend
│ └── [users-api](backend/users-api/README.md)
├─ boms
│ ├── [logging-bom](boms/logging-bom/README.md)
│ └── [testing-bom](boms/testing-bom/README.md)
└─ [collections](collection/README.md)

---

