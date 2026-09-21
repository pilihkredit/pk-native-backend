package com.pk.app.ops.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.adapter.apipartner.ApiPartnerProperties;
import com.pk.core.auth.port.AccountClosureStatusDeviceProvider;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.StoredDevicePayloadReader;
import org.springframework.stereotype.Component;

@Component
public class AccountClosureStatusDeviceProviderImpl implements AccountClosureStatusDeviceProvider {
    private static final String STUB_APP_VERSION = "1.0.0";
    private static final String STUB_PACKAGE = "com.pk.account.closure";
    private static final String STUB_DEVICE_NO = "PK_ACCOUNT_CLOSURE";
    private static final String STUB_PLATFORM = "android";
    private static final String FALLBACK_IP = "127.0.0.1";

    private final ApiPartnerProperties apiPartnerProperties;
    private final ProfileDeviceRepository profileDeviceRepository;
    private final ObjectMapper objectMapper;

    public AccountClosureStatusDeviceProviderImpl(
            ApiPartnerProperties apiPartnerProperties,
            ProfileDeviceRepository profileDeviceRepository,
            ObjectMapper objectMapper
    ) {
        this.apiPartnerProperties = apiPartnerProperties;
        this.profileDeviceRepository = profileDeviceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderDeviceContext create(long userId, String clientIp) {
        String lenderAppName = resolveLenderAppName();
        return profileDeviceRepository.findLatestByUserId(userId)
                .map(stored -> StoredDevicePayloadReader.toLenderDevice(stored, lenderAppName, objectMapper))
                .orElseGet(() -> stubDevice(lenderAppName, clientIp));
    }

    private String resolveLenderAppName() {
        String appName = apiPartnerProperties.appName();
        if (appName == null || appName.isBlank()) {
            return "PKApp";
        }
        return appName.trim();
    }

    private LenderDeviceContext stubDevice(String lenderAppName, String clientIp) {
        DeviceExtendedAttributes extended = new DeviceExtendedAttributes(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                resolveClientIp(clientIp)
        );
        return new LenderDeviceContext(
                lenderAppName,
                STUB_APP_VERSION,
                STUB_PACKAGE,
                STUB_DEVICE_NO,
                STUB_PLATFORM,
                null,
                null,
                lenderAppName,
                extended
        );
    }

    private static String resolveClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return FALLBACK_IP;
        }
        return clientIp.trim();
    }
}
