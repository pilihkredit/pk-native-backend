package com.pk.core.profile;

import java.time.Instant;

/**
 * Baseline face used for 1:1 comparison: latest successful compare, or KYC identity face.
 */
public record FaceComparisonBaseline(String baselineType, String faceEncryptedRef, Instant comparedAt) {
    public static final String TYPE_IDENTITY = "IDENTITY";
    public static final String TYPE_MOBILE_CHANGE = "MOBILE_CHANGE";
    public static final String TYPE_DEVICE_SWITCH_LOGIN = "DEVICE_SWITCH_LOGIN";
    public static final String TYPE_BANK_CARD_ADD = "BANK_CARD_ADD";
}
