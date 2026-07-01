package com.pk.adapter.pendanaan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;

/**
 * Maps Pendanaan raw {@code A000xxx} codes to public {@code L000xxx} API codes.
 * Platform validation uses {@code K000xxx}; never map lender failures to {@code K*} codes.
 */
final class PendanaanLenderCodeMapper {
    private PendanaanLenderCodeMapper() {
    }

    static ApiException toApiException(String lenderCode, String lenderMessage, ProfileSyncModule module) {
        return new ApiException(mapApiCode(lenderCode, module), buildDetail(lenderCode, lenderMessage));
    }

    static ApiCode mapApiCode(String lenderCode, ProfileSyncModule module) {
        if (module == ProfileSyncModule.BANK_CARD) {
            if ("A000104".equals(lenderCode)) {
                return ApiCode.BANK_CARD_VA_NOT_ALLOWED;
            }
            if ("A000339".equals(lenderCode)) {
                return ApiCode.BANK_CARD_ALREADY_BOUND;
            }
            if (isBankCardVerificationFailure(lenderCode)) {
                return ApiCode.BANK_CARD_VERIFICATION_FAILED;
            }
        }
        if (module == ProfileSyncModule.IDENTITY) {
            ApiCode identityCode = mapIdentityCode(lenderCode);
            if (identityCode != null) {
                return identityCode;
            }
        }
        if ("999998".equals(lenderCode) || "999999".equals(lenderCode)) {
            return ApiCode.SERVICE_UNAVAILABLE;
        }
        ApiCode mapped = mapProfileOrBusinessCode(lenderCode);
        if (mapped != null) {
            return mapped;
        }
        if (module == ProfileSyncModule.BANK_CARD) {
            return ApiCode.BANK_CARD_VERIFICATION_FAILED;
        }
        if (module == ProfileSyncModule.IDENTITY) {
            return ApiCode.LENDER_INVALID_REQUEST_PARAMETERS;
        }
        return ApiCode.LENDER_INVALID_REQUEST_PARAMETERS;
    }

    private static ApiCode mapIdentityCode(String lenderCode) {
        return switch (lenderCode) {
            case "A000052" -> ApiCode.LENDER_FACE_RECOGNITION_FAILED;
            case "A000053" -> ApiCode.LENDER_INVALID_EKTP_FORMAT;
            case "A000445" -> ApiCode.LENDER_INVALID_OCR_RAW_DETAIL;
            case "A000455" -> ApiCode.LENDER_IDENTITY_NAME_REQUIRED;
            case "A000456" -> ApiCode.LENDER_IDENTITY_NAME_TOO_LONG;
            default -> null;
        };
    }

    private static ApiCode mapProfileOrBusinessCode(String lenderCode) {
        return switch (lenderCode) {
            case "A000010" -> ApiCode.UPSTREAM_APPLICATION_NOT_FOUND;
            case "A000124" -> ApiCode.CREDIT_LIMIT_NOT_AVAILABLE;
            case "A000145" -> ApiCode.LENDER_LOAN_AMOUNT_REJECTED;
            case "A000063" -> ApiCode.LENDER_INDUSTRY_REQUIRED;
            case "A000069" -> ApiCode.LENDER_INCOME_REQUIRED;
            case "A000078" -> ApiCode.LENDER_INVALID_INCOME_FORMAT;
            case "A000056" -> ApiCode.LENDER_INVALID_EDUCATION_DEGREE;
            case "A000408" -> ApiCode.LENDER_MOTHER_SURNAME_REQUIRED;
            case "A000409" -> ApiCode.LENDER_INVALID_MOTHER_SURNAME_FORMAT;
            case "A000075" -> ApiCode.LENDER_INVALID_EMAIL;
            case "A000144" -> ApiCode.LENDER_DUPLICATE_SUBMISSION_IN_PROGRESS;
            case "A000001", "A000012", "A000017", "A000024" -> ApiCode.LENDER_INVALID_REQUEST_PARAMETERS;
            default -> null;
        };
    }

    private static String buildDetail(String lenderCode, String lenderMessage) {
        if (lenderMessage == null || lenderMessage.isBlank()) {
            return lenderCode;
        }
        return lenderCode + ": " + lenderMessage.trim();
    }

    static boolean isBankCardVerificationFailure(String lenderCode) {
        return switch (lenderCode) {
            case "A000321", "A000322", "A000174", "A000103", "A000105", "A000106", "A000101", "A000102" -> true;
            default -> false;
        };
    }
}
