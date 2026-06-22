# Agent instructions (pk-backend-app)

When editing this repository, follow these rules without exception.

## English only — zero CJK

**Never introduce Chinese, Japanese, or Korean characters** in any file under `pk-backend-app/`.

## No personal identifiers

**Never include personal names, personal emails, or other team-member identifiers** in source, comments, docs, or commit messages you suggest.

Blocked substrings are enforced by `NoPersonalIdentifiersTest` (see `pk-quality/src/test/resources/blocked-personal-substrings.txt`). Add new entries there only when onboarding shared blocklist updates — do not embed names in application code.

Forbidden locations include: source code, comments, tests, YAML/XML/SQL, markdown docs, commit messages you suggest, and generated snippets shown for copy-paste into the repo.

If product requirements are described in Chinese elsewhere (Feishu docs), translate intent into **English** in code and docs here.

## Build gate

After substantive changes, run:

```bash
./mvnw clean verify
```

`pk-quality` tests (`NoCjkTextTest`, `NoPersonalIdentifiersTest`) must pass. Do not claim work is complete if this fails.

## Architecture reminders

- Modular monolith: deploy `pk-app` and `pk-worker` only.
- Layering: `Controller -> ApplicationService -> pk-core -> Port (adapter/infra)`.
- External writes: persist + `outbox_event` in one transaction; worker calls adapter.
- API prefix: `/api/pk/v1`; response `msg` in English.

See [README.md](README.md) and [CONTRIBUTING.md](CONTRIBUTING.md).
