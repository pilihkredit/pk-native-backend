package com.pk.adapter.pendanaan;

import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;

final class PendanaanProfileCodeMapper {
    private PendanaanProfileCodeMapper() {
    }

    static ApiException toApiException(String lenderCode, ProfileSyncModule module) {
        return toApiException(lenderCode, null, module);
    }

    static ApiException toApiException(String lenderCode, String lenderMessage, ProfileSyncModule module) {
        return PendanaanLenderCodeMapper.toApiException(lenderCode, lenderMessage, module);
    }
}
