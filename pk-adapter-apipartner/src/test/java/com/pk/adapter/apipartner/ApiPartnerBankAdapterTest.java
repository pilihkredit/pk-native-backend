package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

class ApiPartnerBankAdapterTest {
    @Test
    void mapsBankListResponse() {
        ApiPartnerHttpClient httpClient = mock(ApiPartnerHttpClient.class);
        ArrayNode data = JsonNodeFactory.instance.arrayNode();
        ObjectNode bca = data.addObject();
        bca.put("bankCode", "BCA");
        bca.put("bankName", "Bank Central Asia");
        when(httpClient.get(
                eq(ApiPartnerBankAdapter.BANK_LIST_PATH),
                eq(ApiPartnerBankAdapter.BUSINESS_TYPE),
                eq(null)
        )).thenReturn(data);

        ApiPartnerBankAdapter adapter = new ApiPartnerBankAdapter(httpClient);

        assertThat(adapter.listBanks()).hasSize(1);
        assertThat(adapter.listBanks().getFirst().bankCode()).isEqualTo("BCA");
    }
}
