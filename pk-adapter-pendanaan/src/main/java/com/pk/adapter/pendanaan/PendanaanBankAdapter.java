package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.reference.BankReference;
import com.pk.core.reference.port.LenderBankPort;
import java.util.ArrayList;
import java.util.List;

public class PendanaanBankAdapter implements LenderBankPort {
    static final String BANK_LIST_PATH = PendanaanOpenApiPaths.BANK_LIST;
    static final String BUSINESS_TYPE = "REFERENCE_BANK_LIST";

    private final PendanaanHttpClient httpClient;

    public PendanaanBankAdapter(PendanaanHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public List<BankReference> listBanks() {
        JsonNode data = httpClient.get(BANK_LIST_PATH, BUSINESS_TYPE, null);
        if (data == null || !data.isArray()) {
            return List.of();
        }
        List<BankReference> banks = new ArrayList<>(data.size());
        for (JsonNode item : data) {
            String bankCode = item.path("bankCode").asText("");
            String bankName = item.path("bankName").asText("");
            if (bankCode.isBlank() || bankName.isBlank()) {
                continue;
            }
            banks.add(new BankReference(bankCode, bankName, null, null));
        }
        return List.copyOf(banks);
    }
}
