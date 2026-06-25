package com.pk.app.area.dto.response;

public record AreaListItemResponse(
        String areaCode,
        String areaName,
        String parentCode,
        int level
) {
}
