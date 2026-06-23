package com.pk.infra.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.security")
public class ProfileProperties {
    private String fieldEncryptionKey = "local-dev-field-encryption-key-32b";

    public String fieldEncryptionKey() {
        return fieldEncryptionKey;
    }

    public void setFieldEncryptionKey(String fieldEncryptionKey) {
        this.fieldEncryptionKey = fieldEncryptionKey;
    }
}
