# Fulfillment Colocation Management System — Assessment Solution

![Java CI & Code Coverage (>80%)](https://github.com/sagarkoshti97-wq/fcs-interview-code-assignment/actions/workflows/main.yml/badge.svg)

## Quick Navigation
* **Code Assignment Requirements:** [java-assignment/CODE_ASSIGNMENT.md](java-assignment/CODE_ASSIGNMENT.md)
* **Technical & Architecture Q&A:** [java-assignment/QUESTIONS.md](java-assignment/QUESTIONS.md)
* **Domain Briefing:** [case-study/BRIEFING.md](case-study/BRIEFING.md)
* **Case Study Scenarios & Analysis:** [case-study/CASE_STUDY.md](case-study/CASE_STUDY.md)
* **Java Application README:** [java-assignment/README.md](java-assignment/README.md)

---

## Setup, Execution & Testing Instructions

### Prerequisites
* **Java:** JDK 17 or JDK 21 LTS
* **Docker:** Docker Desktop running (used by Quarkus DevServices / Testcontainers to provision PostgreSQL automatically during development and testing)
  * *Note for newer Docker Engines (v29+):* If Testcontainers reports `client version 1.32 is too old`, add `api.version=1.44` to `~/.docker-java.properties` or pass `-Dapi.version=1.44`.

### 1. Run Unit & Integration Tests with >80% JaCoCo Coverage Gate
From the `java-assignment` directory:
```bash
cd java-assignment
./mvnw clean verify -Duser.timezone=UTC
