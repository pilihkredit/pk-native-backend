# Contributing to pk-backend-app

## English-only repository (mandatory)

**No Chinese (or any CJK script) is allowed anywhere in this repository.**

This applies to all current and future work, including:

- Java source, tests, and annotations
- Comments and Javadoc
- Log messages and exception messages exposed to operators
- Configuration (`application.yml`, `.properties`, Flyway scripts)
- SQL DDL/DML and migration files
- README, CONTRIBUTING, AGENTS, and other project docs
- Git commit messages (recommended; not enforced by CI)
- Cursor rules and agent instructions under `.cursor/`

User-facing API `msg` fields returned to the mobile app must stay **English** per the interface specification (`v0.4.x`). Locale-specific display strings are produced by the backend as `*Display` fields for Indonesian (`id-ID`), not by embedding CJK or Chinese in this codebase.

### What to use instead

| Do not | Do |
|--------|-----|
| Chinese comments | English comments |
| Chinese log text | English log text with structured fields (`traceId`, `applyId`) |
| Chinese README sections | English documentation |
| Inline Chinese for "temporary" notes | English; remove before merge |

### Enforcement

1. **CI / local build**: `./mvnw clean verify` runs `NoCjkTextTest` in `pk-quality` and **fails the build** on any CJK character in tracked project files.
2. **Pre-commit** (optional but recommended):

   ```bash
   git config core.hooksPath .githooks
   ```

3. **Manual check**:

   ```bash
   ./mvnw -pl pk-quality test
   ```

### Review checklist

Before opening a PR or merging:

- [ ] `./mvnw clean verify` passes
- [ ] No CJK in new or changed files
- [ ] New user-visible strings are English (or `*Display` formatting logic only)
