package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;
import org.junit.jupiter.api.Test;

class ApiPartnerLenderCodeMapperTest {
    @Test
    void mapsUnfinishedLoanBankCardSwitchFailure() {
        ApiException exception = ApiPartnerLenderCodeMapper.toApiException(
                "A000181",
                "Unfinished loan application",
                null
        );

        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_SWITCH_NOT_ALLOWED);
    }

    @Test
    void mapsGenericLenderParameterErrorToL000001() {
        ApiException exception = ApiPartnerLenderCodeMapper.toApiException(
                "A000001",
                "Invalid request parameters",
                ProfileSyncModule.PERSONAL
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(exception.detail()).isEqualTo("Invalid request parameters");
    }

    @Test
    void mapsLenderIndustryRequiredToL000063() {
        ApiException exception = ApiPartnerLenderCodeMapper.toApiException(
                "A000063",
                null,
                ProfileSyncModule.PERSONAL
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INDUSTRY_REQUIRED);
    }

    @Test
    void doesNotMapLenderErrorsToPlatformK000001() {
        ApiException exception = ApiPartnerLenderCodeMapper.toApiException(
                "A000001",
                null,
                ProfileSyncModule.PERSONAL
        );
        assertThat(exception.apiCode().code()).startsWith("L");
        assertThat(exception.apiCode()).isNotEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void mapsUnknownLenderCodeToL000001() {
        ApiCode mapped = ApiPartnerLenderCodeMapper.mapApiCode("A009999", ProfileSyncModule.PERSONAL);
        assertThat(mapped).isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void mapsTongdunDeviceLenderCodes() {
        assertThat(ApiPartnerLenderCodeMapper.mapApiCode("A000466", ProfileSyncModule.TONGDUN_DEVICE))
                .isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(ApiPartnerLenderCodeMapper.mapApiCode("A000467", ProfileSyncModule.TONGDUN_DEVICE))
                .isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(ApiPartnerLenderCodeMapper.mapApiCode("A000468", ProfileSyncModule.TONGDUN_DEVICE))
                .isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
    }
}
