package com.pk.core.profile;

public enum BiometricImageKind {
    ID_CARD("id-card"),
    FACE("face"),
    MOBILE_CHANGE_FACE("mobile-change-face"),
    DEVICE_SWITCH_LOGIN_FACE("device-switch-login-face"),
    BANK_CARD_ADD_FACE("bank-card-add-face");

    private final String objectName;

    BiometricImageKind(String objectName) {
        this.objectName = objectName;
    }

    public String objectName() {
        return objectName;
    }
}
