package com.pk.app.ops.support;

import com.pk.adapter.apipartner.ApiPartnerProperties;
import com.pk.core.auth.port.AccountClosureStatusDeviceProvider;
import com.pk.core.profile.sync.LenderDeviceContext;
import org.springframework.stereotype.Component;

@Component
public class AccountClosureStatusDeviceProviderImpl implements AccountClosureStatusDeviceProvider {
    private static final String STUB_APP_VERSION = "1.0.0";
    private static final String STUB_PACKAGE = "com.pk.account.closure";
    private static final String STUB_DEVICE_NO = "PK_ACCOUNT_CLOSURE";
    private static final String STUB_PLATFORM = "android";

    private final ApiPartnerProperties apiPartnerProperties;

    public AccountClosureStatusDeviceProviderImpl(ApiPartnerProperties apiPartnerProperties) {
        this.apiPartnerProperties = apiPartnerProperties;
    }

    @Override
    public LenderDeviceContext create() {
        String appName = apiPartnerProperties.appName();
        if (appName == null || appName.isBlank()) {
            appName = "PKApp";
        }
        return new LenderDeviceContext(
                appName.trim(),
                STUB_APP_VERSION,
                STUB_PACKAGE,
                STUB_DEVICE_NO,
                STUB_PLATFORM
        );
    }
}
