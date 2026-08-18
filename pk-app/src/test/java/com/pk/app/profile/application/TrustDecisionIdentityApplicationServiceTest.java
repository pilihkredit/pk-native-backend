package com.pk.app.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.adapter.apipartner.ApiPartnerProperties;
import com.pk.app.profile.dto.request.ProfileDeviceRequest;
import com.pk.app.profile.dto.request.TrustDecisionLivenessLicenseRequest;
import com.pk.app.profile.dto.request.TrustDecisionLivenessResultRequest;
import com.pk.app.profile.dto.request.TrustDecisionOcrCheckRequest;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.infra.profile.TrustDecisionIdentityFacade;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class TrustDecisionIdentityApplicationServiceTest {
    private TrustDecisionIdentityFacade facade;
    private TrustDecisionIdentityApplicationService service;
    private AuthenticatedPrincipal principal;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        facade = mock(TrustDecisionIdentityFacade.class);
        ApiPartnerProperties apiPartnerProperties = new ApiPartnerProperties();
        apiPartnerProperties.setAppName("lender-app");
        service = new TrustDecisionIdentityApplicationService(facade, apiPartnerProperties);
        principal = new AuthenticatedPrincipal(10L, "partner-user", "81234567890", 1L);
        request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "trace-1");
        request.addHeader("X-Request-Id", "client-request-1");
    }

    @Test
    void mapsOcrRequestIdentityAndVendorResponse() {
        var parsed = new OcrSessionState.OcrParsedFields(
                "Sample User", "3173010101010001", "MALE", "RELIGION", "SINGLE",
                "1990-01-01", "CITY", "ADDRESS", "JOB", "ID", "O", "LIFETIME",
                "PROVINCE", "CITY", "DISTRICT");
        when(facade.ocrCheck(
                10L, "partner-user", "81234567890", "id-image",
                "client-request-1", "trace-1"
        )).thenReturn(new TrustDecisionIdentityFacade.OcrCheckResult("pass", "ocr-seq", parsed));

        var response = service.ocrCheck(principal, new TrustDecisionOcrCheckRequest("id-image"), request);

        assertThat(response.result()).isEqualTo("pass");
        assertThat(response.sequenceId()).isEqualTo("ocr-seq");
        assertThat(response.ocrName()).isEqualTo("Sample User");
    }

    @Test
    void mapsInteractiveLivenessLicenseRequest() {
        when(facade.obtainLivenessLicense(
                10L, "partner-user", "81234567890", 600, "client-request-1", "trace-1"))
                .thenReturn(new TrustDecisionIdentityFacade.LivenessLicenseResult(
                        "license-value", 1785147934L, "license-seq"));

        var response = service.livenessLicense(
                principal, new TrustDecisionLivenessLicenseRequest(null), request);

        assertThat(response.license()).isEqualTo("license-value");
        assertThat(response.expiryTimestamp()).isEqualTo(1785147934L);
        assertThat(response.sequenceId()).isEqualTo("license-seq");
    }

    @Test
    void mapsInteractiveLivenessResultAndDevice() throws Exception {
        addDeviceHeaders();
        var lenderResponse = new ObjectMapper().readTree("{\"accepted\":true}");
        when(facade.completeInteractiveLiveness(
                eq(10L), eq("partner-user"), eq("81234567890"), eq("final-request"),
                eq("sdk-live-id"), org.mockito.ArgumentMatchers.any(), eq("trace-1")))
                .thenReturn(new TrustDecisionIdentityFacade.FaceRecognitionResult(
                        "final-request", "pass", 0D, "result-seq", "COMPLETED", lenderResponse));

        var response = service.livenessResult(
                principal,
                new TrustDecisionLivenessResultRequest("final-request", "sdk-live-id", device()),
                request);

        assertThat(response.result()).isEqualTo("pass");
        assertThat(response.sequenceId()).isEqualTo("result-seq");
        assertThat(response.moduleStatus()).isEqualTo("COMPLETED");
        assertThat(response.lenderResponse()).isEqualTo(lenderResponse);
    }

    private void addDeviceHeaders() {
        request.addHeader("X-Device-No", "device-1");
        request.addHeader("X-App-Version", "1.2.3");
        request.addHeader("X-Platform", "android");
        request.addHeader("X-App-Package", "com.example.app");
    }

    private static ProfileDeviceRequest device() {
        return new ProfileDeviceRequest(
                "client-app", "1.2.3", "com.example.app", "device-1", "android",
                "brand", "model", null, "14", "store", 8, 8_000L, 64_000L,
                "ad-id", "channel", null, null, null, "127.0.0.1", Map.of("key", "value"));
    }
}
