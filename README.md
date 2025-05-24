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

### Backends

- [users-api](backend/users-api/README.md)

### BOMs

- [logging-bom](boms/logging-bom/README.md)
- [testing-bom](boms/testing-bom/README.md)

### Bruno Collections

- [collections](collection/README.md)

---

## 🚢 Release

Note: You'll need to configure your GitHub credentials in your Maven settings to deploy. Add the following to your
`~/.m2/settings.xml`:

```xml

<activeProfiles>
    <activeProfile>github</activeProfile>
</activeProfiles>

<profiles>
<profile>
    <id>github</id>
    <repositories>
        <repository>
            <id>central</id>
            <url>https://repo1.maven.org/maven2</url>
        </repository>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/smauel/smauel-java</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
</profile>
</profiles>

<servers>
<server>
    <id>github</id>
    <username>USERNAME</username>
    <password>TOKEN</password>
</server>
</servers>
```

Your GitHub token needs to have the `write:packages` scope to deploy packages.

---

```bash
mvn deploy
```

