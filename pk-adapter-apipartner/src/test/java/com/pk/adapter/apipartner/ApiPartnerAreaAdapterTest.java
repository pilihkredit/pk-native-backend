package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.reference.AreaReference;
import org.junit.jupiter.api.Test;

class ApiPartnerAreaAdapterTest {
    @Test
    void mapsAreaListResponse() throws Exception {
        ApiPartnerHttpClient httpClient = mock(ApiPartnerHttpClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode data = JsonNodeFactory.instance.arrayNode();
        ObjectNode jakarta = data.addObject();
        jakarta.put("code", "110000");
        jakarta.put("name", "Jakarta");
        jakarta.put("parentCode", "");
        when(httpClient.post(
                eq(ApiPartnerAreaAdapter.AREA_LIST_PATH),
                eq("{}"),
                eq(ApiPartnerAreaAdapter.BUSINESS_TYPE),
                eq("")
        )).thenReturn(data);

        ApiPartnerAreaAdapter adapter = new ApiPartnerAreaAdapter(httpClient, objectMapper);

        assertThat(adapter.listAreas(null))
                .containsExactly(new AreaReference("110000", "Jakarta", "", 0));
    }
}
