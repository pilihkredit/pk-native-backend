# Contributing to pk-backend-app

## English-only repository (mandatory)

**No Chinese (or any CJK script) is allowed anywhere in this repository.**

**No personal names or personal email addresses** may appear in repository files (source, comments, docs, config). Git commit authors are configured locally and are outside file scans; still avoid signing commits with personal identifiers in messages or `Co-authored-by` trailers when possible.

This applies to all current and future work, including:

- Java source, tests, and annotations
- Comments and Javadoc
- Log messages and exception messages exposed to operators
- Configuration (`application.yml`, `.properties`, Flyway scripts)
- SQL DDL/DML and migration files
- README, CONTRIBUTING, AGENTS, and other project docs
- Git commit messages (recommended; enforced by pre-commit for CJK in staged files)
- Agent instructions in [AGENTS.md](AGENTS.md) (local `.cursor/` is gitignored)

User-facing API `msg` fields returned to the mobile app must stay **English** per the interface specification (`v0.4.x`). Locale-specific display strings are produced by the backend as `*Display` fields for Indonesian (`id-ID`), not by embedding CJK or Chinese in this codebase.

### What to use instead

| Do not | Do |
|--------|-----|
| Chinese comments | English comments |
| Chinese log text | English log text with structured fields (`traceId`, `applyId`) |
| Chinese README sections | English documentation |
| Inline Chinese for "temporary" notes | English; remove before merge |
| Personal names or emails in code/comments | Role-based attribution only; use local `blocked-personal-substrings.local.txt` (gitignored) |

### Enforcement

1. **CI / local build**: `./mvnw clean verify` runs `pk-quality` gates (`NoCjkTextTest`, `NoPersonalIdentifiersTest`) and **fails the build** on violations.
2. **Pre-commit** (recommended):

   ```bash
   git config core.hooksPath .githooks
   ```

   The hook blocks staged `target/`, IDE metadata, CJK text, and personal identifiers before each commit.

3. **Manual check**:

   ```bash
   ./mvnw -pl pk-quality test
   ```

### Ignored paths (do not force-add)

`target/`, `.idea/`, `.vscode/`, `.cursor/`, `*.iml`, `*.log`, `.DS_Store`, `blocked-personal-substrings.local.txt`, local env files — see `.gitignore`.

### Local personal-identifier gate (optional, never commit names)

```bash
cp blocked-personal-substrings.local.txt.example blocked-personal-substrings.local.txt
# Edit the local file with substrings to block (file is gitignored)
```

Or: `export PK_QUALITY_BLOCKED_SUBSTRINGS='substring1,substring2'`

### Review checklist

Before opening a PR or merging:

- [ ] `./mvnw clean verify` passes
- [ ] No CJK in new or changed files
- [ ] No personal names or emails in new or changed files
- [ ] New user-visible strings are English (or `*Display` formatting logic only)
