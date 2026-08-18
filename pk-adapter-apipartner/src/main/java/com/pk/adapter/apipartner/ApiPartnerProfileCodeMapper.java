package com.pk.adapter.apipartner;

import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;

final class ApiPartnerProfileCodeMapper {
    private ApiPartnerProfileCodeMapper() {
    }

    static ApiException toApiException(String lenderCode, ProfileSyncModule module) {
        return toApiException(lenderCode, null, module);
    }

    static ApiException toApiException(String lenderCode, String lenderMessage, ProfileSyncModule module) {
        return ApiPartnerLenderCodeMapper.toApiException(lenderCode, lenderMessage, module);
    }
}
