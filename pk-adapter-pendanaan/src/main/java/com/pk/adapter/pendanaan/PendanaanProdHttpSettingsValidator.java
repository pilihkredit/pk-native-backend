package com.pk.adapter.pendanaan;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
class PendanaanProdHttpSettingsValidator {
    PendanaanProdHttpSettingsValidator(PendanaanProperties properties) {
        properties.validateHttpSettings();
    }
}
