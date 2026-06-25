package com.pk.adapter.pendanaan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;

final class PendanaanProfileCodeMapper {
    private PendanaanProfileCodeMapper() {
    }

    static ApiException toApiException(String lenderCode, boolean bankCardModule) {
        if (bankCardModule) {
            if ("A000104".equals(lenderCode)) {
                return new ApiException(ApiCode.BANK_CARD_VA_NOT_ALLOWED);
            }
            if ("A000339".equals(lenderCode)) {
                return new ApiException(ApiCode.BANK_CARD_ALREADY_BOUND);
            }
            if (isBankCardVerificationFailure(lenderCode)) {
                return new ApiException(ApiCode.BANK_CARD_VERIFICATION_FAILED);
            }
        }
        if ("999998".equals(lenderCode) || "999999".equals(lenderCode)) {
            return new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        if ("A000001".equals(lenderCode) || "A000012".equals(lenderCode) || "A000017".equals(lenderCode)
                || "A000024".equals(lenderCode)) {
            return new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if ("A000144".equals(lenderCode)) {
            return new ApiException(ApiCode.DUPLICATE_SUBMISSION_IN_PROGRESS);
        }
        if (bankCardModule) {
            return new ApiException(ApiCode.BANK_CARD_VERIFICATION_FAILED);
        }
        return new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }

    private static boolean isBankCardVerificationFailure(String lenderCode) {
        return switch (lenderCode) {
            case "A000321", "A000322", "A000174", "A000103", "A000105", "A000106", "A000101", "A000102" -> true;
            default -> false;
        };
    }
}
