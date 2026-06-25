package com.pk.core.reference;

public record AreaReference(
        String areaCode,
        String areaName,
        String parentCode,
        int level
) {
}
