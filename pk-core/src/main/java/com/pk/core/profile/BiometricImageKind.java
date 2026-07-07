package com.pk.core.profile;

public enum BiometricImageKind {
    ID_CARD("id-card"),
    FACE("face");

    private final String objectName;

    BiometricImageKind(String objectName) {
        this.objectName = objectName;
    }

    public String objectName() {
        return objectName;
    }
}
