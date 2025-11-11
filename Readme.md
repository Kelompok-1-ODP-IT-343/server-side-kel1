# Griya Springboot Application — Quick Start Guide

This document explains how to run a Spring Boot application of Griya Project with
**Flyway database migration** 
and the **best practices** for writing migration scripts (PostgreSQL).

---

# 1) Prerequisites

- **Java 21+**
- **Maven** or **Gradle**
- **PostgreSQL 13+** (local or via Docker)
- **Flyway** (integrated via Spring Boot starter)
- (Optional) **Docker + Docker Compose**

---

# 2) Set up application.properties (example)

Change the postgres host, username, password folowing your local database env 

```properties
spring.datasource.url=jdbc:postgresql://localhost:<port>/griya_db
spring.datasource.username= <username>
spring.datasource.password= <password>
```

### About `spring.flyway.baseline-on-migrate=true`
- **Use this when** the database already has schema/tables before adopting Flyway. Flyway will create a baseline (`flyway_schema_history`) and treat the existing state as the starting version.
- **Do not enable** this on a fresh/empty database. For a new DB, keep it `false` and let `V1__...` scripts build the schema from scratch.

---

# 3) Running the Application

### 3.1. Run PostgreSQL with Docker (optional)

Start with:
```bash
docker compose up -d
```

### 3.2. Run Spring Boot

**Maven**
```bash
./mvnw spring-boot:run
```

When the application starts, Flyway will:
1. Create the `flyway_schema_history` table if it doesn’t exist.
2. Run all migration scripts in `classpath:db/migration` in **version order**.

---

# 4) Migration Structure & Naming

### 4.1. Folder Location
```
src/main/resources/db/migration/
```

### 4.2. Types of Migrations
- **Versioned**: `V1__init_schema.sql`, `V2__add_user_table.sql`
- **Repeatable**: `R__refresh_views.sql` (re-applied whenever file changes)

### 4.3. Naming Convention
```
V{version}__{description}.sql
# examples:
V1__init_schema.sql
V2_1__add_indexes.sql     # Flyway supports dot/underscore as separators
R__rebuild_materialized_views.sql
```

**Tips for description**: short, clear, snake/kebab case, meaningful.

### 4.4. Content Guidelines
- **One logical change per file** (e.g., add a table, add an index).
- **Use UPPERCASE for ENUM labels** for consistency.
- **Schema-qualified names** if you use non-default schema:  
  `CREATE TABLE ${appSchema}.users (...);`
- **SQL should be logically idempotent** (no unexpected side effects if re-run in other environments).

---
### 5. Code Formating
- You can simply formating using spotless plugin
- How to use : go to maven plugin then click spotless and click ```spotless:apply```

