# TrustDecision KYC Integration Design

## Summary

Add TrustDecision Indonesia OCR, liveness detection, and face comparison as a second identity verification flow. The existing Advance.ai flow remains available and unchanged at the API boundary. The frontend selects which provider-specific flow to call and owns fallback decisions.

TrustDecision uses separate API routes and provider-specific session state. Both providers share only the final identity persistence and lender synchronization component. Every TrustDecision vendor call is audited, including technical failures and successful calls whose business result is `fail`.

## Goals

- Integrate the TrustDecision Indonesia OCR, liveness, and face comparison APIs.
- Preserve the existing Advance.ai API contract and runtime behavior.
- Expose separate TrustDecision routes so the frontend can select the provider.
- Store TrustDecision call audits in `ocr_vendor_call_log` with an unambiguous provider channel.
- Reuse the existing encrypted biometric storage, identity persistence, onboarding update, and lender synchronization behavior.
- Keep TrustDecision and Advance.ai workflow sessions isolated.
- Prevent provider credentials and raw biometric data from appearing in plaintext logs or database audit payloads.

## Non-Goals

- Backend-driven fallback between TrustDecision and Advance.ai.
- Local score or similarity threshold enforcement.
- TrustDecision Dukcapil identity verification.
- Changes to existing Advance.ai public endpoints.
- Database schema changes to `ocr_vendor_call_log`.

## Vendor APIs

Use the TrustDecision Indonesia node for all calls:

| Operation | Endpoint |
| --- | --- |
| OCR | `https://id.apitd.net/verification/kyc/ocr/v1` |
| Liveness | `https://id.apitd.net/verification/kyc/liveness/v1` |
| Face comparison | `https://id.apitd.net/verification/kyc/identity/v1` |

Authentication uses vendor-issued partner credentials. Credentials must be supplied through runtime environment variables and must not have repository defaults. The vendor requires credentials as query parameters, but audit endpoints and application logs must omit the complete query string.

All requests use country code `ID`.

## Architecture

### Core Port

Add a provider-specific `TrustDecisionKycPort` in `pk-core`. It defines three operations:

- OCR accepts an identity card image and returns normalized KTP fields, the vendor result, the vendor sequence ID, optional document forgery information, and sanitized raw response data.
- Liveness accepts a face image and returns the vendor result, score, and sequence ID.
- Face comparison accepts identity card and face images and returns the vendor result, similarity, and sequence ID.

The port must represent vendor API success separately from the vendor business result. A `code = 200` liveness or face comparison response with `result = fail` is a completed vendor call, not a technical failure.

### Infrastructure Adapter

Add a `TrustDecisionKycClient` in `pk-infra`. It is responsible for:

- Constructing TrustDecision requests.
- Applying provider credentials without exposing them to logs.
- Enforcing connection and read timeouts.
- Parsing and validating response envelopes.
- Mapping technical failures to platform API errors.
- Writing one audit row for every attempted vendor operation.

Add `TrustDecisionProperties` with an independent enable flag, endpoint configuration, credential fields, image-size limit, timeouts, and session TTL. Production credentials are supplied only through environment variables.

### Provider Workflow

Add `TrustDecisionIdentityFacade` in `pk-infra` to coordinate the three TrustDecision steps. It uses provider-specific Redis session state and does not reuse the Advance.ai Redis key.

Extract the final identity completion behavior from the existing `IdentityOcrFacade` into a shared component. This component owns:

- Final identity validation and persistence.
- Sensitive field encryption.
- Encrypted identity card and face image storage.
- Profile version and onboarding updates.
- Lender payload construction and synchronization.
- Final request idempotency.

Advance.ai and TrustDecision call this shared component only after satisfying their provider-specific prerequisites. Provider response parsing remains outside the shared component.

### Application Layer

Add a dedicated TrustDecision application service, controller, request DTOs, and response DTOs in `pk-app`. Register the controller only when TrustDecision is enabled.

The existing Advance.ai controller and DTOs remain unchanged.

## API Design

All paths are relative to `/api/v1`.

### OCR Check

`POST /profile/identity/tongdun/ocr-check`

Request:

```json
{
  "imageBase64": "..."
}
```

The response contains normalized Indonesian KTP fields compatible with the existing identity workflow. It may also include the TrustDecision `result` and `sequenceId`, but must not expose raw vendor payloads or encrypted storage references.

Behavior:

- Always write an `OCR_CHECK` audit row after attempting the vendor call.
- When `code = 200` and `result = success` with usable `card_info`, store the normalized OCR state and encrypted identity card reference in the TrustDecision Redis session.
- When `code = 200` and `result = fail`, retain the audit row and stop the TrustDecision flow. Do not call the lender.
- Treat a successful envelope without usable required KTP fields as an OCR no-result failure.

### Liveness Check

`POST /profile/identity/tongdun/liveness-check`

Request:

```json
{
  "faceImageBase64": "..."
}
```

The response contains `result`, `score`, and `sequenceId`. It does not contain a local threshold.

Behavior:

- Require a valid TrustDecision OCR session for the same authenticated user.
- Always write a `LIVENESS_CHECK` audit row after attempting the vendor call.
- When `code = 200`, save the completed liveness state whether `result` is `pass` or `fail`.
- Do not compare the returned score against a local threshold.
- A technical failure stops the TrustDecision flow and does not call the lender.

The liveness image from this request is audited using an encrypted image reference. The final face comparison request supplies its own images and does not depend on this image as its comparison input.

### Face Recognition

`POST /profile/identity/tongdun/face-recognition`

Request:

```json
{
  "requestId": "...",
  "faceImageBase64": "...",
  "idCardImageBase64": "...",
  "device": {}
}
```

The response contains `requestId`, TrustDecision `result`, `similarity`, `sequenceId`, `moduleStatus`, and the lender response. It does not contain a local threshold.

Behavior:

- Require successful OCR output and a technically completed liveness call in the TrustDecision session.
- Always write a `FACE_COMPARE` audit row after attempting the vendor call.
- When `code = 200`, continue final identity persistence and lender synchronization whether `result` is `pass` or `fail`.
- Do not compare similarity against a local threshold.
- A technical failure stops final persistence and lender synchronization.
- Repeated final submissions with the same `requestId` must not duplicate the lender request.

## Workflow State

TrustDecision state is stored in Redis under a provider-qualified key. The key must include both the authenticated user ID and the stable provider code `trustDecision`.

The state contains:

- OCR completion status.
- Liveness technical completion status.
- OCR normalized fields.
- Sanitized OCR raw data required for the lender payload.
- OCR and liveness vendor results and sequence IDs.
- Encrypted image references.
- Relevant vendor audit row IDs.
- Last update timestamp.

The default TTL is 30 minutes. No plaintext image bytes are stored in Redis.

Advance.ai and TrustDecision sessions must not overwrite or satisfy each other's prerequisites.

## Persistence And Lender Synchronization

Every vendor attempt writes to `ocr_vendor_call_log`:

| Field | TrustDecision value |
| --- | --- |
| `channel` | `trustDecision` |
| `operation_type` | `OCR_CHECK`, `LIVENESS_CHECK`, or `FACE_COMPARE` |
| `vendor_code` | TrustDecision response code |
| `vendor_message` | Truncated TrustDecision message |
| `score` | Liveness score or face similarity when returned |
| `endpoint` | Endpoint without query parameters |
| `request_json` | Sanitized request metadata with encrypted image references |
| `response_json` | Sanitized response with sensitive values encrypted in place |

The TrustDecision `sequence_id` must remain queryable in the stored sanitized response. No schema change is required because the existing `channel` field already identifies the vendor.

Intermediate OCR and liveness completion does not call the lender. The final face operation calls the shared completion component only when:

- OCR returned usable `card_info`.
- Liveness completed with a technically successful API response.
- Face comparison completed with a technically successful API response.

The liveness and face business results may be `pass` or `fail`; neither blocks lender synchronization. Their numeric scores do not affect the workflow.

After final completion:

- Set `user_profile_identity.ocr_channel` to `trustDecision`.
- Store the latest TrustDecision OCR audit ID in `ocr_vendor_call_log_id`.
- Store the provider-normalized, protected OCR result in `ocr_result_json`.
- Persist final identity card and face images through `BiometricImageStore`.
- Build lender `rawOcrDetail` through a TrustDecision-specific mapper rather than the Advance.ai envelope helpers.

## Error Handling

The following are technical failures and stop the TrustDecision flow:

- Connection failure or timeout.
- HTTP error.
- TrustDecision response code other than `200`.
- Invalid or incomplete response envelope.
- Image decoding, validation, or size failure.
- Missing required OCR `card_info`.

For liveness and face comparison, `code = 200` with `result = fail` is not a technical failure and does not stop the flow.

All platform response messages remain English. Existing API error codes should be reused where their meanings match. Add provider-neutral error codes only when existing OCR errors cannot accurately represent the condition.

Audit persistence failure must not replace or hide the original vendor or business exception. It is logged without credentials or biometric content.

## Security And Privacy

- Never commit partner credentials or provide non-empty credential defaults.
- Never include credential query parameters in endpoint logs, exception messages, metrics tags, or audit rows.
- Redact or encrypt identity numbers, names, addresses, images, and vendor-returned portrait data before storage in audit JSON.
- Store biometric images only through the existing encrypted storage abstraction.
- Apply the existing biometric and identity retention policy to TrustDecision data.
- Do not return raw vendor payloads, encrypted references, or credentials to clients.
- Keep request body logging disabled or sanitized for the new routes.

## Configuration

Add environment-backed configuration equivalent to:

- `PK_TRUSTDECISION_ENABLED`
- `PK_TRUSTDECISION_PARTNER_CODE`
- `PK_TRUSTDECISION_PARTNER_KEY`
- `PK_TRUSTDECISION_OCR_URL`
- `PK_TRUSTDECISION_LIVENESS_URL`
- `PK_TRUSTDECISION_FACE_COMPARISON_URL`
- `PK_TRUSTDECISION_CONNECT_TIMEOUT_MS`
- `PK_TRUSTDECISION_READ_TIMEOUT_MS`
- `PK_TRUSTDECISION_MAX_IMAGE_BYTES`
- `PK_TRUSTDECISION_SESSION_TTL`

Endpoint defaults may point to the documented Indonesia node. Credential defaults must be empty. Enabling TrustDecision with missing credentials must fail startup with an English configuration error.

## Testing

### Adapter Tests

- Verify request field mapping and country code `ID`.
- Verify OCR KTP field normalization.
- Verify OCR forgery and portrait response handling.
- Verify liveness and face `pass` and `fail` parsing.
- Verify timeouts, HTTP errors, non-`200` codes, malformed JSON, and missing required fields.
- Verify credential query parameters are absent from captured logs and audit endpoints.
- Verify sensitive request and response content is encrypted or redacted.

### Workflow Tests

- OCR success creates only the TrustDecision session and audit record.
- OCR `result = fail` writes an audit record and stops the flow.
- Liveness `pass` and `fail` both allow face comparison.
- Face comparison `pass` and `fail` both call final persistence and lender synchronization.
- Technical liveness or face failure prevents lender synchronization.
- TrustDecision state cannot satisfy Advance.ai prerequisites and vice versa.
- Final completion persists `ocr_channel = trustDecision` and the correct OCR audit ID.
- Duplicate final `requestId` does not duplicate lender synchronization.

### Regression And Build Verification

- Preserve existing Advance.ai controller and behavior tests.
- Add application API tests for disabled and enabled TrustDecision routes.
- Run `./mvnw clean verify` after implementation so module tests and repository quality gates pass.

## Rollout

1. Deploy with TrustDecision disabled and credentials supplied only in the target runtime secret store.
2. Enable in the test environment and validate all three operations against the Indonesia node.
3. Confirm audit rows use `channel = trustDecision` and contain no credential values.
4. Confirm both `pass` and `fail` liveness and face results reach lender synchronization after OCR success.
5. Enable the frontend provider selection and fallback behavior.
6. Monitor technical failure rates, latency, OCR no-result rates, and lender synchronization outcomes by provider channel.

Rollback requires disabling `PK_TRUSTDECISION_ENABLED`. The Advance.ai endpoints remain available throughout rollout.
