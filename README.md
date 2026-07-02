# pk-backend-app

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
| `pk-infra` | MySQL, Redis/queue/OSS integration (planned) |
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

# Local real lender (profile local + seeded pk_provider in Docker MySQL)
docker compose up -d mysql redis
SPRING_PROFILES_ACTIVE=local ./mvnw -pl pk-app spring-boot:run
SPRING_PROFILES_ACTIVE=local ./mvnw -pl pk-worker spring-boot:run
```

## Database

Schema is managed **manually** (no Flyway). With Docker MySQL for local dev:

```bash
docker compose up -d mysql redis
docker compose ps          # wait until healthy
```

Local credentials match `application-local.yml`: database `pk`, user `pk`, password `pk`, port `3306`.  
On **first** MySQL volume init, `sql/create_pk_schema.sql` runs automatically (all tables plus Pendanaan test gateway seed data).

Without Docker:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pk DEFAULT CHARSET utf8mb4"
mysql -u root -p pk < sql/create_pk_schema.sql
```

Schema changes are made by editing `sql/create_pk_schema.sql` and applying the full script in test/prod through your release process.

## Configuration

Active profile: `SPRING_PROFILES_ACTIVE` — `local` (default), `test`, or `prod`.

| Profile | Use | Database | Notes |
|---------|-----|----------|--------|
| `local` | Developer machine | `localhost:3306/pk` (built-in defaults) | `com.pk` DEBUG logs; worker outbox **off** |
| `test` | Staging / QA | **`PK_DB_URL` / `PK_DB_USERNAME` / `PK_DB_PASSWORD` required** | Worker outbox **on** |
| `prod` | Production | Same env vars as test | Stricter logging; actuator exposes `health` only |

```bash
# Local (default)
./mvnw -pl pk-app spring-boot:run

# Test
SPRING_PROFILES_ACTIVE=test \
  PK_DB_URL='jdbc:mysql://test-host:3306/pk?...' \
  PK_DB_USERNAME=pk PK_DB_PASSWORD=*** \
  ./mvnw -pl pk-app spring-boot:run

# Production
SPRING_PROFILES_ACTIVE=prod PK_DB_URL=... PK_DB_USERNAME=... PK_DB_PASSWORD=... \
  java -jar pk-app/target/pk-app-*.jar
```

| Variable | Local default | Test / Prod |
|----------|---------------|-------------|
| `SPRING_PROFILES_ACTIVE` | `local` | `test` or `prod` |
| `PK_DB_URL` | in `application-local.yml` | **required** |
| `PK_DB_USERNAME` | `pk` | **required** |
| `PK_DB_PASSWORD` | `pk` | **required** |
| `PK_APP_PORT` | `8080` | optional |
| `PK_WORKER_PORT` | `8081` | optional |

Config files: `pk-app` and `pk-worker` each have `application.yml` + `application-{local,test,prod}.yml`.

### Lender (Pendanaan) configuration

By default (`pk.lender.config.source=db`), Pendanaan credentials are loaded at startup from `pk_provider` and `pk_api_credential`. Local Docker init applies the test seed at the end of `sql/create_pk_schema.sql`. Production values must be inserted manually in the database.

### Partner callback URLs (register with Pendanaan)

`pk_provider.callback_base_url` must match the public API prefix (no trailing slash). Pendanaan calls:

| Environment | `callback_base_url` | Server |
|-------------|---------------------|--------|
| Test | `https://api-test.pilihkredit.id/api/v1` | 147.139.188.108 |
| Prod | `https://api.pilihkredit.id/api/v1` | 8.215.70.226 |

| Purpose | Test URL | Prod URL |
|---------|----------|----------|
| OAuth token | `https://api-test.pilihkredit.id/api/v1/oauth/token` | `https://api.pilihkredit.id/api/v1/oauth/token` |
| Credit callback | `https://api-test.pilihkredit.id/api/v1/callback/credit/result` | `https://api.pilihkredit.id/api/v1/callback/credit/result` |
| Loan callback | `https://api-test.pilihkredit.id/api/v1/callback/loan/result` | `https://api.pilihkredit.id/api/v1/callback/loan/result` |

Set `pk_api_credential.callback_client_id` and `callback_secret_ref` to the values you give Pendanaan (same pair as `PK_CALLBACK_*` when using env override). Restart `pk-app` after updating credentials.

Set `pk.lender.config.source=env` to fall back to YAML / environment variables only (e.g. local `fake` mode without DB rows).

| Variable | Default | Purpose |
|----------|---------|---------|
| `PK_LENDER_CONFIG_SOURCE` | `db` | `db` = load from MySQL; `env` = YAML/env only |
| `PK_LENDER_CONFIG_PROVIDER_CODE` | `pendanaan` | `pk_provider.provider_code` row to load |

## API prefix

Client APIs use `/api/v1` (see interface documentation v0.4.x). Controllers declare paths relative to this prefix (e.g. `@RequestMapping("/auth")`).

Health check: `GET /api/v1/platform/status`

## Source rules (mandatory)

**English only — no Chinese or CJK anywhere in this repository. No personal names or emails in repository files.**

Applies to source, comments, logs, config, SQL, docs, and future development. See [CONTRIBUTING.md](CONTRIBUTING.md) and [AGENTS.md](AGENTS.md).

Enforcement:

```bash
./mvnw clean verify          # CJK + personal-identifier gates (pk-quality)
./mvnw -pl pk-quality test   # gates only

# Recommended: pre-commit blocks build/IDE paths and CJK
git config core.hooksPath .githooks
```

## Git hygiene

Never commit `target/`, IDE folders (`.idea/`, `.vscode/`, `.cursor/`), logs, or secrets. See `.gitignore`. The pre-commit hook rejects staged build or IDE paths.

## Stack

- Java 21
- Spring Boot 3.3.5
- Maven multi-module + Maven Wrapper
- **MyBatis 3** (pure, SQL in XML) — migrating from Spring `JdbcTemplate`
- Aliyun RDS MySQL 8.0 (target)

## Persistence (MyBatis)

Data access lives in `pk-infra`. Domain ports stay in `pk-core`; implementations use MyBatis.

```
pk-infra/
  src/main/java/com/pk/infra/
    {domain}/
      mapper/          # MyBatis @Mapper interfaces (no SQL here)
        XxxMapper.java
      repository/      # Port implementations (@Repository)
        XxxRepositoryImpl.java
    mybatis/
      config/MybatisInfraConfiguration.java   # @MapperScan("com.pk.infra.**.mapper")
      typehandler/InstantTypeHandler.java
      typehandler/BooleanTinyintTypeHandler.java
  src/main/resources/
    mapper/
      {domain}/
        XxxMapper.xml  # All SQL statements
```

Runtime processes (`pk-app` and `pk-worker`) declare `mybatis.mapper-locations` and `type-handlers-package` in their own `application.yml` files.

**Call chain (unchanged):** `Facade` → `pk-core` Port → `{domain}.repository.*RepositoryImpl` → `{domain}.mapper.*Mapper` → XML

**Migration status:** All `pk-infra` database repositories use MyBatis. `DatabaseConnectionChecker` still uses `JdbcTemplate` for health `SELECT 1` only.
