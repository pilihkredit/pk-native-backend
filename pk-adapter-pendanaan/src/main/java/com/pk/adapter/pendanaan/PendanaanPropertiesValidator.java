package com.pk.adapter.pendanaan;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class PendanaanPropertiesValidator {
    private final PendanaanProperties properties;

    public PendanaanPropertiesValidator(PendanaanProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void validate() {
        properties.validateHttpSettings();
    }
}
