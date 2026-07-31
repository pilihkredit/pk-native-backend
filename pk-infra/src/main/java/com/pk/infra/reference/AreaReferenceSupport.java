package com.pk.infra.reference;

public final class AreaReferenceSupport {
    private AreaReferenceSupport() {
    }

    public static String normalizeParentCode(String parentCode) {
        return parentCode == null || parentCode.isBlank() ? "" : parentCode.trim();
    }
}
