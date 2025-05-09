# Raspy API Server

> View the full API reference at our [Documentaion Site](https://raspy-team.github.io/raspy-api/).

---

## Tech Stack

| Item        | Details                                                         |
| ----------- | --------------------------------------------------------------- |
| Java        | **17** — Automatically built with Java 17+ if available locally |
| Kotlin      | 1.9.25                                                          |
| Gradle      | 8.5 (Recommended: use wrapper via `./gradlew`)                  |
| Spring Boot | 3.4.5                                                           |
| IDE         | IntelliJ IDEA (Ultimate or Education edition is recommended)    |

---

## Environment Configuration

### Profiles & Secret Management

| Environment | Method                                                 |
| ----------- | ------------------------------------------------------ |
| Docker      | Requires `.env` file (values are injected via Compose) |
| Local       | Requires `application-secret.yml`                      |

> To request the required configuration files, please contact: **[xhae000@gmail.com](mailto:xhae000@gmail.com)**

---

### Active Profiles

`application.yml`

```yaml
spring:
  profiles:
    active: dev,secret  # options: dev + secret, or prod
```

### Notes

* Use **`application-dev.yml`** for local/dev environments, and **`application-prod.yml`** for production.
* Ensure profiles are never mixed (e.g., avoid using prod profile locally).
* Secret or sensitive configuration (e.g., DB credentials) **must not be committed** or exposed in any public Git repository.

---

## Commit Convention

> Refer to the following rules for commit formatting and conventions:

![img.png](img.png)
