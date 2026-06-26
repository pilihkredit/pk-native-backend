package com.pk.infra.reference;

final class AreaReferenceSupport {
    private AreaReferenceSupport() {
    }

    static String normalizeParentCode(String parentCode) {
        return parentCode == null || parentCode.isBlank() ? "" : parentCode.trim();
    }
}
