package com.pk.infra.loan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.loan.LenderLoanProduct;
import java.util.List;

final class ProductSnapshotPayloadCodec {
    private final ObjectMapper objectMapper;

    ProductSnapshotPayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String encode(String productStatus, List<LenderLoanProduct> products) {
        try {
            return objectMapper.writeValueAsString(new StoredPayload(productStatus, products));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to encode product snapshot", exception);
        }
    }

    StoredPayload decode(String productsJson) {
        try {
            return objectMapper.readValue(productsJson, StoredPayload.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to decode product snapshot", exception);
        }
    }

    record StoredPayload(String productStatus, List<LenderLoanProduct> products) {
        List<LenderLoanProduct> toLenderProducts() {
            return products == null ? List.of() : products;
        }
    }
}
