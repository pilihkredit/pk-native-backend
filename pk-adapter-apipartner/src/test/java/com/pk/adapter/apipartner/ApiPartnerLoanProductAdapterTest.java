package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.loan.port.LenderLoanProductPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiPartnerLoanProductAdapterTest {
    @Mock
    private ApiPartnerHttpClient httpClient;

    @Test
    void mapsTopLevelFieldsAndUnevenRateRawJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiPartnerLoanProductAdapter adapter = new ApiPartnerLoanProductAdapter(httpClient, objectMapper);
        when(httpClient.postWithInteraction(
                eq(ApiPartnerLoanProductAdapter.PRODUCT_LIST_PATH),
                org.mockito.ArgumentMatchers.anyString(),
                eq(ApiPartnerLoanProductAdapter.BUSINESS_TYPE),
                eq("APPLY-1")
        )).thenReturn(new ApiPartnerHttpClient.ExchangeResult(objectMapper.readTree("""
                {
                  "applyId": "APPLY-1",
                  "creditApplyNo": "CA2025060200001",
                  "userId": "USR202506020001",
                  "creditStatus": "SUCCESS",
                  "productStatus": "READY",
                  "products": [
                    {
                      "productCode": "PD001",
                      "productName": "Cash Loan",
                      "minAmount": 100000.00,
                      "maxAmount": 5000000.00,
                      "comprehensiveRateUnit": "M",
                      "comprehensiveRate": 0.18,
                      "repayMethods": [
                        {
                          "repayMethod": "RP001",
                          "cycleType": "D",
                          "cycleInterval": 15,
                          "cycleCount": 6,
                          "totalCycleInterval": 6,
                          "repayMethodType": 1,
                          "unevenBillsRepaymentRate": "[{\\"termNum\\":1,\\"repaymentRate\\":0.6},{\\"termNum\\":2,\\"repaymentRate\\":0.4}]"
                        }
                      ]
                    }
                  ]
                }
                """), 99L));

        LenderLoanProductPort.LenderLoanProductListResult result = adapter.listProducts("APPLY-1");

        assertThat(result.applyId()).isEqualTo("APPLY-1");
        assertThat(result.creditApplyNo()).isEqualTo("CA2025060200001");
        assertThat(result.userId()).isEqualTo("USR202506020001");
        assertThat(result.externalCreditStatus()).isEqualTo("SUCCESS");
        assertThat(result.productStatus()).isEqualTo("READY");
        assertThat(result.externalInteractionId()).isEqualTo(99L);
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().comprehensiveRateUnit()).isEqualTo("M");
        assertThat(result.products().getFirst().repayMethods()).hasSize(1);
        assertThat(result.products().getFirst().repayMethods().getFirst().unevenBillsRepaymentRateRaw())
                .contains("termNum");
        assertThat(result.products().getFirst().repayMethods().getFirst().unevenBillsRepaymentRates())
                .hasSize(2);
        assertThat(result.products().getFirst().repayMethods().getFirst().unevenBillsRepaymentRates().getFirst().termNum())
                .isEqualTo(1);
    }
}
