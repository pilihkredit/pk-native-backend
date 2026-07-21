package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.onboarding.OnboardingModuleCode;
import com.pk.core.profile.port.LenderProfileQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnboardingProgressFacadeTest {
    private LenderProfileQueryPort lenderProfileQueryPort;
    private OnboardingProgressFacade facade;

    @BeforeEach
    void setUp() {
        lenderProfileQueryPort = mock(LenderProfileQueryPort.class);
        facade = new OnboardingProgressFacade(lenderProfileQueryPort, new ObjectMapper());
    }

    @Test
    void returnsIncompleteWhenLenderModulesMissing() {
        when(lenderProfileQueryPort.query(any())).thenReturn(
                new LenderProfileQueryPort.LenderProfileQueryResult("""
                        {
                          "partnerUserId": "U10001",
                          "userId": "USR-1",
                          "mobileNo": { "mobileNo": "081234567890" },
                          "profile": null,
                          "contact": null,
                          "identity": null,
                          "bankCard": null,
                          "device": null
                        }
                        """)
        );

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_INCOMPLETE);
        assertThat(result.missingModules()).containsExactly(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE,
                OnboardingModuleCode.IDENTITY
        );
        assertThat(result.completedModules()).isEmpty();
    }

    @Test
    void returnsIncompleteWhenIdentityMissingOnLender() {
        when(lenderProfileQueryPort.query(any())).thenReturn(
                new LenderProfileQueryPort.LenderProfileQueryResult("""
                        {
                          "partnerUserId": "U10001",
                          "profile": { "educationDegree": 1, "industry": 16, "income": "5000000" },
                          "contact": { "userContacts": [{ "mobileNo": "81234567891" }] },
                          "bankCard": { "bankCode": "BCA", "cardNumber": "****1234" },
                          "device": { "deviceNo": "device-1" },
                          "identity": null
                        }
                        """)
        );

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_INCOMPLETE);
        assertThat(result.completedModules()).containsExactlyInAnyOrder(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE
        );
        assertThat(result.missingModules()).containsExactly(OnboardingModuleCode.IDENTITY);
    }

    @Test
    void returnsSyncedWhenAllLenderModulesPresent() {
        when(lenderProfileQueryPort.query(any())).thenReturn(
                new LenderProfileQueryPort.LenderProfileQueryResult("""
                        {
                          "partnerUserId": "U10001",
                          "profile": { "educationDegree": 1 },
                          "contact": { "userContacts": [] },
                          "bankCard": { "bankCode": "BCA" },
                          "device": { "deviceNo": "device-1" },
                          "identity": { "name": "JOHN DOE", "idNo": "3201010101010001" }
                        }
                        """)
        );

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_SYNCED);
        assertThat(result.missingModules()).isEmpty();
        assertThat(result.completedModules()).containsExactlyInAnyOrder(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE,
                OnboardingModuleCode.IDENTITY
        );
    }

    @Test
    void propagatesLenderUserNotFound() {
        when(lenderProfileQueryPort.query(any())).thenThrow(
                new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND, "data tidak ada")
        );

        assertThatThrownBy(() -> facade.getProgress(1L, "U10001"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
    }

    @Test
    void treatsEmptyObjectAsMissing() {
        when(lenderProfileQueryPort.query(any())).thenReturn(
                new LenderProfileQueryPort.LenderProfileQueryResult("""
                        {
                          "profile": {},
                          "contact": { "userContacts": [{ "mobileNo": "81234567891" }] },
                          "bankCard": { "bankCode": "BCA" },
                          "device": { "deviceNo": "device-1" },
                          "identity": { "name": "JOHN DOE" }
                        }
                        """)
        );

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_INCOMPLETE);
        assertThat(result.missingModules()).containsExactly(OnboardingModuleCode.PERSONAL);
    }
}
