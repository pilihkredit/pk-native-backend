package com.pk.app.agreement.dto.response;

public record ProductSummaryFieldsResponse(
        String publisherName,
        String productType,
        String productDescription,
        String provisionFeeDisplay
) {
}
