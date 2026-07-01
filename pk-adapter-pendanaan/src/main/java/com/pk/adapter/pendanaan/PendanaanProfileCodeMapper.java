package com.pk.adapter.pendanaan;

import com.pk.core.api.ApiException;

final class PendanaanProfileCodeMapper {
    private PendanaanProfileCodeMapper() {
    }

    static ApiException toApiException(String lenderCode, boolean bankCardModule) {
        return toApiException(lenderCode, null, bankCardModule);
    }

    static ApiException toApiException(String lenderCode, String lenderMessage, boolean bankCardModule) {
        return PendanaanLenderCodeMapper.toApiException(lenderCode, lenderMessage, bankCardModule);
    }
}
