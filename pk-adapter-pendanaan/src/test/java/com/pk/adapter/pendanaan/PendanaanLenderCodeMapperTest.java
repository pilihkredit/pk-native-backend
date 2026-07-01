package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.Test;

class PendanaanLenderCodeMapperTest {
    @Test
    void mapsGenericLenderParameterErrorToL000001() {
        ApiException exception = PendanaanLenderCodeMapper.toApiException("A000001", "请求参数错误", false);
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(exception.detail()).isEqualTo("A000001: 请求参数错误");
    }

    @Test
    void mapsLenderIndustryRequiredToL000063() {
        ApiException exception = PendanaanLenderCodeMapper.toApiException("A000063", null, false);
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INDUSTRY_REQUIRED);
    }

    @Test
    void doesNotMapLenderErrorsToPlatformK000001() {
        ApiException exception = PendanaanLenderCodeMapper.toApiException("A000001", null, false);
        assertThat(exception.apiCode().code()).startsWith("L");
        assertThat(exception.apiCode()).isNotEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void mapsUnknownLenderCodeToL000001() {
        ApiCode mapped = PendanaanLenderCodeMapper.mapApiCode("A009999", false);
        assertThat(mapped).isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
    }
}
