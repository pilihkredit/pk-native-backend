package com.pk.adapter.apipartner;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.LenderBankCardPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class ApiPartnerBankCardAdapterTest {
    @Test
    void postsSetDefaultBankCardRequest() {
        ApiPartnerHttpClient httpClient = Mockito.mock(ApiPartnerHttpClient.class);
        ApiPartnerBankCardAdapter adapter = new ApiPartnerBankCardAdapter(httpClient, new ObjectMapper());

        adapter.setDefaultBankCard(new LenderBankCardPort.SetDefaultBankCardCommand("partner-1", 10001L));

        verify(httpClient).post(
                ArgumentMatchers.eq(ApiPartnerOpenApiPaths.USER_BANK_CARD_DEFAULT),
                ArgumentMatchers.eq("{\"partnerUserId\":\"partner-1\",\"bankCardId\":10001}"),
                ArgumentMatchers.eq("USER_BANK_CARD_DEFAULT"),
                ArgumentMatchers.eq("partner-1")
        );
    }
}
