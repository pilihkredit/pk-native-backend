# pk-backend

Modular monolith backend for the PK aggregation platform, aligned with [pk technical architecture](https://ek8l1y505u.feishu.cn/wiki/ClEowIu2OiFYyIknYhec9Hf8nJe).

## Runtime processes

| Process | Port (default) | Responsibility |
|---------|----------------|----------------|
| `pk-app` | 8080 | Client API, admin API, PK callback intake, synchronous query orchestration |
| `pk-worker` | 8081 | Outbox consumption, profile sync, credit/loan submit, callback post-processing, polling, reconciliation |

Other Maven modules are code boundaries only, not separate deployables.

## Modules

| Module | Role |
|--------|------|
| `pk-app` | HTTP entry: validation, DTO assembly, fast persist + enqueue |
| `pk-worker` | Async jobs by queue: `callback` > `submit` > `poll` > `reconcile` |
| `pk-core` | Domain models, credit/loan state machines, ports, error codes |
| `pk-infra` | MySQL, Flyway, Redis/queue/OSS integration (planned) |
| `pk-adapter-pendanaan` | Pendanaan OpenAPI mapping, OAuth, callback parsing |
| `pk-quality` | Dependency and CJK text gates |

## Layering (`pk-app`)

```
Controller -> ApplicationService -> Domain (pk-core) -> Port (adapter / infra)
```

External write path: Controller -> ApplicationService -> business table + `outbox_event` (same transaction) -> `pk-worker` -> adapter.

## Commands

```bash
./mvnw clean verify
./mvnw -pl pk-app spring-boot:run
./mvnw -pl pk-worker spring-boot:run
```

## Database

Reference DDL: `sql/create_pk_schema.sql`. Flyway is wired in `pk-infra` but disabled until migrations are added.

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pk DEFAULT CHARSET utf8mb4"
mysql -u root -p pk < sql/create_pk_schema.sql
```

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `PK_DB_URL` | `jdbc:mysql://localhost:3306/pk` | MySQL JDBC URL |
| `PK_DB_USERNAME` | `pk` | Database user |
| `PK_DB_PASSWORD` | `pk` | Database password |
| `PK_APP_PORT` | `8080` | `pk-app` HTTP port |
| `PK_WORKER_PORT` | `8081` | `pk-worker` HTTP port |

## API prefix

Client APIs use `/api/pk/v1` (see interface documentation v0.4.x).

Health check: `GET /api/pk/v1/platform/status`

## Source rules

- Project source, config, logs, and docs use English only (no CJK in repo).
- Run `./mvnw -pl pk-quality test` before opening a change.

## Stack

- Java 21
- Spring Boot 3.3.5
- Maven multi-module + Maven Wrapper
- Aliyun RDS MySQL 8.0 (target)
