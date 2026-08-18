package com.pk.adapter.apipartner;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
class ApiPartnerProdHttpSettingsValidator {
    ApiPartnerProdHttpSettingsValidator(ApiPartnerProperties properties) {
        properties.validateHttpSettings();
    }
}
