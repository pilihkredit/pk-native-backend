package com.pk.app.credit.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record SliderSegmentResponse(
        BigDecimal value,
        String valueDisplay,
        String segmentType
) {
}
