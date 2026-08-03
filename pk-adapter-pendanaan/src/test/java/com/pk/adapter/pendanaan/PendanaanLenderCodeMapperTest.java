package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;
import org.junit.jupiter.api.Test;

class PendanaanLenderCodeMapperTest {
    @Test
    void mapsUnfinishedLoanBankCardSwitchFailure() {
        ApiException exception = PendanaanLenderCodeMapper.toApiException(
                "A000181",
                "Unfinished loan application",
                null
        );

        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_SWITCH_NOT_ALLOWED);
    }

    @Test
    void mapsGenericLenderParameterErrorToL000001() {
        ApiException exception = PendanaanLenderCodeMapper.toApiException(
                "A000001",
                "请求参数错误",
                ProfileSyncModule.PERSONAL
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(exception.detail()).isEqualTo("请求参数错误");
    }

    @Test
    void mapsLenderIndustryRequiredToL000063() {
        ApiException exception = PendanaanLenderCodeMapper.toApiException(
                "A000063",
                null,
                ProfileSyncModule.PERSONAL
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INDUSTRY_REQUIRED);
    }

    @Test
    void doesNotMapLenderErrorsToPlatformK000001() {
        ApiException exception = PendanaanLenderCodeMapper.toApiException(
                "A000001",
                null,
                ProfileSyncModule.PERSONAL
        );
        assertThat(exception.apiCode().code()).startsWith("L");
        assertThat(exception.apiCode()).isNotEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void mapsUnknownLenderCodeToL000001() {
        ApiCode mapped = PendanaanLenderCodeMapper.mapApiCode("A009999", ProfileSyncModule.PERSONAL);
        assertThat(mapped).isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void mapsTongdunDeviceLenderCodes() {
        assertThat(PendanaanLenderCodeMapper.mapApiCode("A000466", ProfileSyncModule.TONGDUN_DEVICE))
                .isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(PendanaanLenderCodeMapper.mapApiCode("A000467", ProfileSyncModule.TONGDUN_DEVICE))
                .isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(PendanaanLenderCodeMapper.mapApiCode("A000468", ProfileSyncModule.TONGDUN_DEVICE))
                .isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
    }
}
