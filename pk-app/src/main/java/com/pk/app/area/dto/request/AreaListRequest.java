package com.pk.app.area.dto.request;

import jakarta.validation.constraints.Size;

public record AreaListRequest(
        @Size(max = 32) String parentCode
) {
}
