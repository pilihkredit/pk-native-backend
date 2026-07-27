# TrustDecision KYC Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add provider-specific TrustDecision Indonesia OCR, liveness, and face comparison APIs with isolated workflow state, complete vendor auditing, shared final identity persistence, and unchanged Advance.ai endpoints.

**Architecture:** A new TrustDecision port and HTTP adapter handle provider protocol details. A provider-specific facade and Redis store coordinate the three calls, while a shared identity completion service owns final persistence and lender synchronization for both providers. Separate application routes let the frontend choose the provider.

**Tech Stack:** Java 21, Spring Boot 3.3, Java HttpClient, Redis, MyBatis, Jackson, JUnit 5, Mockito, AssertJ.

---

### Task 1: Add TrustDecision Core Contracts

**Files:**
- Create: `pk-core/src/main/java/com/pk/core/profile/port/TrustDecisionKycPort.java`
- Create: `pk-core/src/main/java/com/pk/core/profile/port/TrustDecisionSessionStore.java`
- Create: `pk-core/src/main/java/com/pk/core/profile/ocr/TrustDecisionSessionState.java`
- Test: `pk-core/src/test/java/com/pk/core/profile/ocr/TrustDecisionSessionStateTest.java`

- [ ] **Step 1: Write the failing state contract test**

```java
@Test
void distinguishesTechnicalCompletionFromBusinessResult() {
    var state = TrustDecisionSessionState.afterLiveness(
            TrustDecisionSessionState.afterOcr(sampleOcr()), "fail", 0.41, "live-seq");
    assertThat(state.ocrCompleted()).isTrue();
    assertThat(state.livenessCompleted()).isTrue();
    assertThat(state.livenessResult()).isEqualTo("fail");
}
```

- [ ] **Step 2: Run the test and verify the missing types fail compilation**

Run: `./mvnw -pl pk-core -Dtest=TrustDecisionSessionStateTest test`

Expected: FAIL because the TrustDecision contracts do not exist.

- [ ] **Step 3: Add provider result records and immutable session state**

```java
public interface TrustDecisionKycPort {
    OcrResult checkIdentityCard(byte[] image);
    LivenessResult checkLiveness(byte[] image);
    FaceCompareResult compareFaces(byte[] idCardImage, byte[] faceImage);

    record OcrResult(String result, String sequenceId, String rawJson,
                     OcrSessionState.OcrParsedFields parsed) {}
    record LivenessResult(String result, double score, String sequenceId) {}
    record FaceCompareResult(String result, double similarity, String sequenceId) {}
}
```

The session record must retain normalized OCR data, protected raw OCR JSON, encrypted image references, audit IDs, liveness technical completion, result, score, sequence ID, and update time. `TrustDecisionSessionStore` exposes `find`, `save`, and `delete` by user ID.

- [ ] **Step 4: Run core tests**

Run: `./mvnw -pl pk-core test`

Expected: PASS.

- [ ] **Step 5: Commit core contracts**

```bash
git add pk-core/src/main/java/com/pk/core/profile pk-core/src/test/java/com/pk/core/profile
git commit -m "feat: add TrustDecision KYC contracts"
```

### Task 2: Add TrustDecision Configuration And HTTP Client

**Files:**
- Create: `pk-infra/src/main/java/com/pk/infra/ocr/TrustDecisionProperties.java`
- Create: `pk-infra/src/main/java/com/pk/infra/ocr/TrustDecisionHttpSupport.java`
- Create: `pk-infra/src/main/java/com/pk/infra/ocr/TrustDecisionKycClient.java`
- Create: `pk-infra/src/main/java/com/pk/infra/ocr/TrustDecisionOcrParser.java`
- Test: `pk-infra/src/test/java/com/pk/infra/ocr/TrustDecisionKycClientTest.java`
- Test: `pk-infra/src/test/java/com/pk/infra/ocr/TrustDecisionOcrParserTest.java`

- [ ] **Step 1: Write failing parser and local HTTP server tests**

Cover OCR `success`, OCR `fail`, liveness `pass` and `fail`, face `pass` and `fail`, non-200 vendor codes, invalid JSON, HTTP errors, and timeouts. Capture the request and assert `country` is `ID` and images are base64 without a data URI prefix.

```java
assertThat(result.result()).isEqualTo("fail");
assertThat(result.score()).isEqualTo(0.41);
verify(logWriter).write(argThat(entry ->
        "trustDecision".equals(entry.channel())
                && entry.endpoint().equals(serverBaseUrl + "/liveness")));
```

- [ ] **Step 2: Run focused tests and verify failure**

Run: `./mvnw -pl pk-infra -Dtest=TrustDecisionKycClientTest,TrustDecisionOcrParserTest test`

Expected: FAIL because the adapter classes do not exist.

- [ ] **Step 3: Implement configuration validation**

Use prefix `pk.trustdecision`. Defaults may include the three Indonesia endpoint URLs, timeouts, 3 MiB image size, and a 30-minute session TTL. Credentials default to empty strings. When enabled, reject blank credentials with an English startup error.

- [ ] **Step 4: Implement client calls and audit persistence**

Build vendor URLs only at send time:

```java
URI requestUri = TrustDecisionHttpSupport.authenticatedUri(
        endpoint, properties.partnerCode(), properties.partnerKey());
String auditEndpoint = TrustDecisionHttpSupport.withoutQuery(requestUri);
```

Every attempted operation writes an `OcrVendorCallLogEntry` with channel `trustDecision`. Store vendor `sequence_id` in sanitized response JSON. Never include credentials in the audit endpoint, exception, or application log.

- [ ] **Step 5: Run focused tests**

Run: `./mvnw -pl pk-infra -Dtest=TrustDecisionKycClientTest,TrustDecisionOcrParserTest test`

Expected: PASS.

- [ ] **Step 6: Commit the adapter**

```bash
git add pk-infra/src/main/java/com/pk/infra/ocr pk-infra/src/test/java/com/pk/infra/ocr
git commit -m "feat: add TrustDecision KYC client"
```

### Task 3: Add Provider-Isolated Redis Session Storage

**Files:**
- Create: `pk-infra/src/main/java/com/pk/infra/ocr/RedisTrustDecisionSessionStore.java`
- Test: `pk-infra/src/test/java/com/pk/infra/ocr/RedisTrustDecisionSessionStoreTest.java`

- [ ] **Step 1: Write a failing key and serialization test**

```java
store.save(42L, state);
verify(redis.opsForValue()).set(
        eq("pk:ocr:session:trustDecision:42"), anyString(), eq(Duration.ofMinutes(30)));
```

- [ ] **Step 2: Run the focused test and verify failure**

Run: `./mvnw -pl pk-infra -Dtest=RedisTrustDecisionSessionStoreTest test`

Expected: FAIL because the store does not exist.

- [ ] **Step 3: Implement JSON serialization and provider-qualified keys**

Use a fixed `trustDecision` segment and the configured TTL. Convert malformed cached state into an empty result after deleting the invalid key. Do not store image bytes or credentials.

- [ ] **Step 4: Run the focused test**

Run: `./mvnw -pl pk-infra -Dtest=RedisTrustDecisionSessionStoreTest test`

Expected: PASS.

- [ ] **Step 5: Commit the session store**

```bash
git add pk-infra/src/main/java/com/pk/infra/ocr/RedisTrustDecisionSessionStore.java pk-infra/src/test/java/com/pk/infra/ocr/RedisTrustDecisionSessionStoreTest.java
git commit -m "feat: isolate TrustDecision KYC sessions"
```

### Task 4: Extract Shared Final Identity Completion

**Files:**
- Create: `pk-infra/src/main/java/com/pk/infra/profile/IdentityVerificationCompletionService.java`
- Create: `pk-infra/src/main/java/com/pk/infra/profile/IdentityVerificationCompletionCommand.java`
- Modify: `pk-infra/src/main/java/com/pk/infra/profile/IdentityOcrFacade.java`
- Modify: `pk-infra/src/main/java/com/pk/infra/profile/ProfileInfraConfiguration.java`
- Test: `pk-infra/src/test/java/com/pk/infra/profile/IdentityVerificationCompletionServiceTest.java`
- Modify: `pk-infra/src/test/java/com/pk/infra/profile/IdentityOcrFacadeBasicSaveTest.java`

- [ ] **Step 1: Write failing completion and Advance.ai regression tests**

Verify final completion stores encrypted image references, the supplied channel and OCR audit ID, builds the supplied lender raw OCR detail, updates the device and KYC state, and returns the lender response. Verify duplicate completed `requestId` returns idempotently without another lender call.

- [ ] **Step 2: Run focused tests and verify failure**

Run: `./mvnw -pl pk-infra -Dtest=IdentityVerificationCompletionServiceTest,IdentityOcrFacadeBasicSaveTest test`

Expected: FAIL because the completion service does not exist.

- [ ] **Step 3: Implement a provider-neutral completion command**

```java
public record IdentityVerificationCompletionCommand(
        String requestId,
        String channel,
        Long ocrVendorCallLogId,
        OcrSessionState.OcrParsedFields parsed,
        String lenderRawOcrDetail,
        byte[] idCardImage,
        byte[] faceImage,
        LenderDeviceContext device
) {}
```

Move only the final common persistence and lender synchronization behavior. Keep provider parsing, session checks, and score handling in provider facades.

- [ ] **Step 4: Delegate existing Advance.ai final completion**

Update `IdentityOcrFacade.faceRecognition` to call the shared service with channel `advanceAi`. Preserve its public result shape and behavior.

- [ ] **Step 5: Run all profile and OCR tests**

Run: `./mvnw -pl pk-infra -Dtest='*Identity*Test,*Ocr*Test' test`

Expected: PASS.

- [ ] **Step 6: Commit the extraction**

```bash
git add pk-infra/src/main/java/com/pk/infra/profile pk-infra/src/test/java/com/pk/infra/profile
git commit -m "refactor: share identity verification completion"
```

### Task 5: Implement TrustDecision Workflow Facade

**Files:**
- Create: `pk-infra/src/main/java/com/pk/infra/profile/TrustDecisionIdentityFacade.java`
- Create: `pk-infra/src/main/java/com/pk/infra/ocr/TrustDecisionLenderRawOcrDetailBuilder.java`
- Test: `pk-infra/src/test/java/com/pk/infra/profile/TrustDecisionIdentityFacadeTest.java`
- Test: `pk-infra/src/test/java/com/pk/infra/ocr/TrustDecisionLenderRawOcrDetailBuilderTest.java`

- [ ] **Step 1: Write the failing workflow tests**

Verify these exact decisions:

```java
verify(completionService, never()).complete(any()); // OCR result=fail
verify(completionService).complete(any());          // liveness result=fail, face result=fail
verify(completionService, never()).complete(any()); // liveness technical error
```

Also verify missing/expired prerequisites raise `OCR_SESSION_INVALID` and invalid OCR KTP data raises the existing format or no-result error.

- [ ] **Step 2: Run focused tests and verify failure**

Run: `./mvnw -pl pk-infra -Dtest=TrustDecisionIdentityFacadeTest,TrustDecisionLenderRawOcrDetailBuilderTest test`

Expected: FAIL because the workflow does not exist.

- [ ] **Step 3: Implement the three-step orchestration**

OCR saves session state only when usable KTP data exists. Liveness marks technical completion for both `pass` and `fail`. Face comparison calls completion for both `pass` and `fail`, provided all three API calls technically completed.

- [ ] **Step 4: Implement lender raw OCR mapping**

Build JSON containing the normalized TrustDecision KTP fields and protected provider raw detail in the lender-compatible shape. Do not invoke Advance.ai envelope helpers.

- [ ] **Step 5: Run focused tests**

Run: `./mvnw -pl pk-infra -Dtest=TrustDecisionIdentityFacadeTest,TrustDecisionLenderRawOcrDetailBuilderTest test`

Expected: PASS.

- [ ] **Step 6: Commit the workflow**

```bash
git add pk-infra/src/main/java/com/pk/infra/profile/TrustDecisionIdentityFacade.java pk-infra/src/main/java/com/pk/infra/ocr/TrustDecisionLenderRawOcrDetailBuilder.java pk-infra/src/test/java/com/pk/infra
git commit -m "feat: add TrustDecision identity workflow"
```

### Task 6: Expose Provider-Specific Application APIs

**Files:**
- Create: `pk-app/src/main/java/com/pk/app/profile/controller/TrustDecisionIdentityController.java`
- Create: `pk-app/src/main/java/com/pk/app/profile/application/TrustDecisionIdentityApplicationService.java`
- Create: `pk-app/src/main/java/com/pk/app/profile/dto/request/TrustDecisionOcrCheckRequest.java`
- Create: `pk-app/src/main/java/com/pk/app/profile/dto/request/TrustDecisionLivenessCheckRequest.java`
- Create: `pk-app/src/main/java/com/pk/app/profile/dto/request/TrustDecisionFaceRecognitionRequest.java`
- Create: `pk-app/src/main/java/com/pk/app/profile/dto/response/TrustDecisionOcrCheckResponse.java`
- Create: `pk-app/src/main/java/com/pk/app/profile/dto/response/TrustDecisionLivenessCheckResponse.java`
- Create: `pk-app/src/main/java/com/pk/app/profile/dto/response/TrustDecisionFaceRecognitionResponse.java`
- Test: `pk-app/src/test/java/com/pk/app/profile/application/TrustDecisionIdentityApplicationServiceTest.java`

- [ ] **Step 1: Write failing application mapping tests**

Assert authenticated identity, trace ID, client request ID, device header mapping, request DTO conversion, and response conversion. Responses expose vendor result and score/similarity but no local threshold.

- [ ] **Step 2: Run the focused test and verify failure**

Run: `./mvnw -pl pk-app -am -Dtest=TrustDecisionIdentityApplicationServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: FAIL because application classes do not exist.

- [ ] **Step 3: Implement the application service and controller**

Register routes only when `pk.trustdecision.enabled=true`:

```text
POST /profile/identity/tongdun/ocr-check
POST /profile/identity/tongdun/liveness-check
POST /profile/identity/tongdun/face-recognition
```

Use existing authentication and `ApiResponse` patterns. All validation and response messages remain English.

- [ ] **Step 4: Run focused application tests**

Run: `./mvnw -pl pk-app -am -Dtest=TrustDecisionIdentityApplicationServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: PASS.

- [ ] **Step 5: Commit the API layer**

```bash
git add pk-app/src/main/java/com/pk/app/profile pk-app/src/test/java/com/pk/app/profile
git commit -m "feat: expose TrustDecision identity APIs"
```

### Task 7: Wire Configuration And Security Guards

**Files:**
- Modify: `pk-infra/src/main/java/com/pk/infra/ocr/OcrInfraConfiguration.java`
- Modify: `pk-infra/src/main/java/com/pk/infra/profile/ProfileInfraConfiguration.java`
- Modify: `pk-app/src/main/resources/application.yml`
- Modify: `pk-app/src/main/resources/application-test.yml`
- Modify: `pk-app/src/main/resources/application-prod.yml`
- Test: `pk-infra/src/test/java/com/pk/infra/ocr/TrustDecisionCredentialLeakTest.java`

- [ ] **Step 1: Write failing configuration and secret-leak tests**

Start configuration with enabled and missing credentials and assert an English configuration failure. Execute a captured request and assert the credential value is absent from audit entries, structured log messages, exception messages, and stored endpoint strings.

- [ ] **Step 2: Run focused tests and verify failure**

Run: `./mvnw -pl pk-infra -Dtest=TrustDecisionCredentialLeakTest test`

Expected: FAIL until configuration validation and URL sanitization are wired.

- [ ] **Step 3: Register beans and environment-backed configuration**

Add the approved `PK_TRUSTDECISION_*` variables. Keep credential defaults empty. Enable the feature independently from `pk.ocr.enabled`; the TrustDecision controller requires only its own enable flag and shared infrastructure beans.

- [ ] **Step 4: Run focused and quality tests**

Run: `./mvnw -pl pk-infra -Dtest=TrustDecisionCredentialLeakTest test`

Run: `./mvnw -pl pk-quality test`

Expected: PASS.

- [ ] **Step 5: Commit configuration**

```bash
git add pk-infra/src/main/java/com/pk/infra pk-infra/src/test/java/com/pk/infra pk-app/src/main/resources
git commit -m "feat: configure TrustDecision KYC securely"
```

### Task 8: Full Verification And Documentation

**Files:**
- Modify: `README.md`
- Modify: `docs/superpowers/plans/2026-07-27-trustdecision-kyc-integration.md`

- [ ] **Step 1: Document routes and environment variables**

Document only variable names and behavior. Do not include credential values, personal identifiers, or non-English repository text.

- [ ] **Step 2: Run targeted TrustDecision tests**

Run: `./mvnw -pl pk-core,pk-infra,pk-app -am -Dtest='*TrustDecision*Test' -Dsurefire.failIfNoSpecifiedTests=false test`

Expected: PASS.

- [ ] **Step 3: Run the repository build gate**

Run: `./mvnw clean verify`

Expected: PASS, including `NoCjkTextTest` and `NoPersonalIdentifiersTest` when a local identifier blocklist is configured.

- [ ] **Step 4: Review the final diff for secrets and scope**

Run:

```bash
git diff --check
git status --short
rg -n 'PK_TRUSTDECISION_PARTNER_(CODE|KEY)' README.md pk-app/src/main/resources
```

Expected: no credential values, no whitespace errors, and only approved TrustDecision and shared completion changes.

- [ ] **Step 5: Commit documentation and final verification updates**

```bash
git add README.md docs/superpowers/plans/2026-07-27-trustdecision-kyc-integration.md
git commit -m "docs: document TrustDecision KYC integration"
```
