# Fulfillment Colocation Management System — Assessment Solution

![Java CI & Code Coverage (>80%)](https://github.com/sagarkoshti97-wq/fcs-interview-code-assignment/actions/workflows/main.yml/badge.svg)

![Architecture Overview](docs/screenshots/architecture-overview.png)

## Project & Documentation Links
* **Assignment Specification:** [java-assignment/CODE_ASSIGNMENT.md](java-assignment/CODE_ASSIGNMENT.md)
* **Architecture & Technical Q&A:** [java-assignment/QUESTIONS.md](java-assignment/QUESTIONS.md)
* **Domain Briefing:** [case-study/BRIEFING.md](case-study/BRIEFING.md)
* **Case Study Analysis:** [case-study/CASE_STUDY.md](case-study/CASE_STUDY.md)
* **Java Service Notes:** [java-assignment/README.md](java-assignment/README.md)

---

## Running & Verifying Locally

### Prerequisites
* **JDK 21 LTS** (ensure Eclipse Java Build Path, Compiler compliance, and Maven run configurations all use JDK 21)
* **Docker** running for Quarkus DevServices / Testcontainers PostgreSQL
  * If Docker uses a newer engine API version with Testcontainers, configure `api.version=1.44` in `~/.docker-java.properties` or pass `-Dapi.version=1.44`.

### Run Tests & Enforce >= 80% Coverage
From `java-assignment`:

```powershell
.\mvnw.cmd clean verify "-Duser.timezone=Asia/Kolkata"
