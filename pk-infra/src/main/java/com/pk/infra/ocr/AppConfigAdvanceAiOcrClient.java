package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import org.springframework.data.redis.core.StringRedisTemplate;

public class AppConfigAdvanceAiOcrClient implements AdvanceAiOcrPort {
    private final OcrProviderConfigLoader configLoader;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OcrVendorCallLogWriter callLogWriter;
    private final OcrSensitiveJsonSupport sensitiveJsonSupport;

    public AppConfigAdvanceAiOcrClient(
            OcrProviderConfigLoader configLoader,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter callLogWriter,
            OcrSensitiveJsonSupport sensitiveJsonSupport
    ) {
        this.configLoader = configLoader;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.callLogWriter = callLogWriter;
        this.sensitiveJsonSupport = sensitiveJsonSupport;
    }

    @Override
    public LicenseTokenResult getLicenseToken(Long licenseEffectiveSeconds) {
        return delegate().getLicenseToken(licenseEffectiveSeconds);
    }

    @Override
    public String ocrCheckIdCard(byte[] imageBytes) {
        return delegate().ocrCheckIdCard(imageBytes);
    }

    @Override
    public LivenessResult livenessCheck(String livenessId) {
        return delegate().livenessCheck(livenessId);
    }

    @Override
    public FaceCompareResult compareFaces(byte[] idCardImage, byte[] faceImage) {
        return delegate().compareFaces(idCardImage, faceImage);
    }

    private AdvanceAiOcrClient delegate() {
        return new AdvanceAiOcrClient(
                configLoader.loadAdvanceAi(), redisTemplate, objectMapper, callLogWriter, sensitiveJsonSupport);
    }
}
