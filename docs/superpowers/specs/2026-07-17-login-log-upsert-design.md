# Design: Profile Login Log Sync (`loginLog`)

Date: 2026-07-17  
Status: approved for planning  
Lender API: `POST /api/open/v1/user/info/upsert` (`userInfo.loginLog`)  
Reference: [Pendanaan OpenAPI](https://ek8l1y505u.feishu.cn/wiki/ExC1wwGVWiQ8CqkrRdEcc2ZinKb)

## Goal

Add a partner-facing write API that persists the latest login log locally (same pattern as other `user_profile_*` modules) and synchronously upserts the lender `loginLog` module together with `device` and `mobileNo`.

## Decisions

| Topic | Decision |
|-|-|
| API shape | New standalone write API `POST /profile/login-log` (not hooked into auth login) |
| Device | Required `device` body object, same as personal / bank-card / contacts |
| Persistence | Persist to `user_profile_login_log` |
| Idempotency | One row per `profile_id` (upsert latest); same `requestId` returns previous success |
| Sync mode | Synchronous `syncNow` before success response |
| Onboarding / KYC | **Not** part of onboarding progress or KYC required modules |

## API Contract

### Endpoint

`POST /profile/login-log`

Auth: `Authorization: Bearer {accessToken}` + common client headers.

### Request

| Field | Type | Required | Constraints | Notes |
|-|-|-|-|-|
| `requestId` | String | Yes | max 64 | Idempotency key |
| `loginType` | Integer | Yes | `1` / `2` / `4` / `5` | Password / OTP / Face / Gesture |
| `loginIp` | String | Yes | max 32 | Login IP |
| `loginLat` | BigDecimal | No | — | Latitude |
| `loginLng` | BigDecimal | No | — | Longitude |
| `device` | Object | Yes | existing `ProfileDeviceRequest` | Must match header `X-Device-No` / platform rules |

### Response `data`

| Field | Type | Notes |
|-|-|-|
| `requestId` | String | Echo |
| `moduleStatus` | String | Always `COMPLETED` on success |
| `lenderResponse` | Object | Lender upsert `data` (same style as personal) |

### Error mapping

- Pre-lender validation → `K000xxx` (e.g. `K000001`, `K000017`, device mismatch)
- Lender rejection → `L000xxx` (A000450–A000452 map to `L000001`; other codes via existing lender mapper)

## Data Model

### Table `user_profile_login_log`

Aligned with `user_profile_personal` / `user_profile_bank_card` module tables:

| Column | Type | Notes |
|-|-|-|
| `profile_id` | BIGINT UNSIGNED PK | One row per user |
| `mobile_no` | VARCHAR(32) | Owner mobile |
| `login_type` | INT | Lender `loginType` |
| `login_ip` | VARCHAR(32) | Lender `loginIp` |
| `login_lat` | DECIMAL(10,7) NULL | Optional |
| `login_lng` | DECIMAL(10,7) NULL | Optional |
| `module_status` | VARCHAR(32) | `COMPLETED` on success |
| `last_request_id` | VARCHAR(64) | Idempotency |
| `last_lender_request_json` | JSON NULL | Audit |
| `last_lender_response_json` | JSON NULL | Audit |
| `created_at` / `updated_at` | DATETIME(3) | Standard timestamps |

Index: `idx_user_profile_login_log_mobile_no (mobile_no)`.

SQL deliverables:

1. Add table to `sql/create_pk_schema.sql`
2. Add migrate script `sql/migrate_user_profile_login_log.sql`

## Runtime Flow

```text
App POST /profile/login-log
  -> ProfileController
  -> ProfileApplicationService (resolve device from body + headers)
  -> ProfileServiceFacade.saveLoginLog
       1. validate requestId / loginType / loginIp / lat-lng / device
       2. if existing.lastRequestId == requestId -> return stored success (+ lenderResponse)
       3. upsert user_profile_login_log
       4. UserDeviceWriter.upsertFromRequest (latest device snapshot)
       5. ProfileSyncOrchestrator.syncNow(LOGIN_LOG + payload snapshot)
       6. ProfileSyncHandler persists lender audit JSON on module row
       7. return COMPLETED + lenderResponse
```

Lender request body shape:

```json
{
  "requestId": "...",
  "partnerUserId": "...",
  "userInfo": {
    "mobileNo": "...",
    "loginLog": {
      "loginType": 2,
      "loginIp": "203.0.113.1",
      "loginLat": -6.2,
      "loginLng": 106.8
    },
    "device": { "...": "..." }
  }
}
```

## Code Changes

### Core

- `ProfileSyncModule`: add `LOGIN_LOG`
- `ProfileSyncPayload`: add `LoginLogProfilePayload(loginType, loginIp, loginLat, loginLng)`
- New `ProfileLoginLogData` + `ProfileLoginLogRepository` port

### Adapter (`pk-adapter-pendanaan`)

- `PendanaanProfileUpsertMapper.applyModule`: handle `LOGIN_LOG`
- Build `userInfo.loginLog` fields only (device still via existing `applyDevice`)

### Infra

- `ProfileLoginLogRepositoryImpl` (JDBC upsert / find / updateLastLenderAudit)
- `ProfileServiceFacade.saveLoginLog(...)`
- `ProfileSyncPayloadLoader`: `LOGIN_LOG` requires explicit payload snapshot (same as bank-card/identity)
- `LenderSyncAuditRequestBuilder`: audit `loginLog` from payload or stored row
- `ProfileSyncHandler.persistLenderAudit`: `LOGIN_LOG` branch
- Wire repository bean in profile infra configuration

### App

- DTOs: `ProfileLoginLogSaveRequest` / `ProfileLoginLogSaveResponse`
- `ProfileController` + `ProfileApplicationService.saveLoginLog`

### Explicit non-goals

- Do **not** auto-call from `/auth/otp/verify` or `/auth/password/login` in this change
- Do **not** add `loginLog` to `OnboardingProgressFacade` / KYC required modules
- Do **not** append login history rows

## Testing

- Unit: mapper builds `loginLog`; facade idempotency on same `requestId`; invalid `loginType`/`loginIp` rejected before lender call
- Unit/integration: sync handler writes lender audit on login-log row; device validation failures
- Adapter test: upsert body contains `mobileNo` + `loginLog` + `device`

## Success Criteria

1. App can call `POST /profile/login-log` with device and get `COMPLETED` + lender `data`
2. Latest login log is stored in `user_profile_login_log` with audit JSON after successful lender sync
3. Repeated same `requestId` does not re-call lender and returns previous success payload
4. Lender failures surface as `L*` codes; local validation as `K*` codes
