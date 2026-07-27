package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.pk.core.profile.port.TrustDecisionKycPort;

public class AppConfigTrustDecisionKycClient implements TrustDecisionKycPort {
    private final OcrProviderConfigLoader configLoader;
    private final ObjectMapper objectMapper;
    private final OcrVendorCallLogWriter callLogWriter;
    private final OcrSensitiveJsonSupport sensitiveJsonSupport;

    public AppConfigTrustDecisionKycClient(
            OcrProviderConfigLoader configLoader,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter callLogWriter,
            OcrSensitiveJsonSupport sensitiveJsonSupport
    ) {
        this.configLoader = configLoader;
        this.objectMapper = objectMapper;
        this.callLogWriter = callLogWriter;
        this.sensitiveJsonSupport = sensitiveJsonSupport;
    }

    @Override
    public OcrResult checkIdentityCard(byte[] imageBytes) {
        return delegate().checkIdentityCard(imageBytes);
    }

    @Override
    public LivenessLicense obtainLivenessLicense(int sessionDurationSeconds) {
        return delegate().obtainLivenessLicense(sessionDurationSeconds);
    }

    @Override
    public SdkLivenessResult retrieveLivenessResult(String livenessId) {
        return delegate().retrieveLivenessResult(livenessId);
    }

    private TrustDecisionKycClient delegate() {
        return new TrustDecisionKycClient(
                configLoader.loadTrustDecision(), objectMapper, callLogWriter, sensitiveJsonSupport);
    }
}
