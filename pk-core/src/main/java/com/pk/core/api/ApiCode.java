package com.pk.core.api;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ApiCode {
    SUCCESS("000000", "success", ApiCodeLayer.SUCCESS),

    INVALID_REQUEST_PARAMETERS("K000001", "Invalid request parameters", ApiCodeLayer.PLATFORM_VALIDATION),
    UNAUTHORIZED_REQUEST("K000012", "Unauthorized request", ApiCodeLayer.PLATFORM_VALIDATION),
    DEVICE_NO_REQUIRED("K000017", "deviceNo is required", ApiCodeLayer.PLATFORM_VALIDATION),
    FACE_RECOGNITION_FAILED("K000052", "Face recognition failed", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_EKTP_FORMAT("K000053", "Invalid EKTP format", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_EDUCATION_DEGREE("K000056", "Invalid education degree", ApiCodeLayer.PLATFORM_VALIDATION),
    INDUSTRY_REQUIRED("K000063", "Industry is required", ApiCodeLayer.PLATFORM_VALIDATION),
    INCOME_REQUIRED("K000069", "Income is required", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_PAYDAY("K000070", "Invalid payday", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_INCOME_FORMAT("K000078", "Invalid income format", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_CONTACT_RELATIONSHIP("K000081", "Invalid contact relationship", ApiCodeLayer.PLATFORM_VALIDATION),
    CONTACT_NAME_REQUIRED("K000082", "Contact name is required", ApiCodeLayer.PLATFORM_VALIDATION),
    CONTACT_MOBILE_REQUIRED("K000083", "Contact mobile is required", ApiCodeLayer.PLATFORM_VALIDATION),
    CONTACT_MOBILE_SAME_AS_OWN("K000089", "Contact mobile cannot be your own number", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_MOBILE_NUMBER("K000090", "Invalid mobile number", ApiCodeLayer.PLATFORM_VALIDATION),
    DUPLICATE_SUBMISSION_IN_PROGRESS("K000144", "Duplicate submission in progress", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_LOAN_AMOUNT("K000145", "Invalid loan amount", ApiCodeLayer.PLATFORM_VALIDATION),
    TOO_MANY_REQUESTS("K000201", "Too many requests", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_OR_EXPIRED_VERIFICATION_CODE("K000202", "Invalid or expired verification code", ApiCodeLayer.PLATFORM_VALIDATION),
    OTP_TOKEN_MISMATCH("K000206", "OTP token does not match mobile number", ApiCodeLayer.PLATFORM_VALIDATION),
    PASSWORD_ALREADY_SET("K000210", "Password already set", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_PASSWORD_FORMAT("K000211", "Invalid password format", ApiCodeLayer.PLATFORM_VALIDATION),
    PASSWORD_CONFIRM_MISMATCH("K000212", "Password confirmation does not match", ApiCodeLayer.PLATFORM_VALIDATION),
    PASSWORD_NOT_SET("K000213", "Password not set", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_MOBILE_OR_PASSWORD("K000214", "Invalid mobile number or password", ApiCodeLayer.PLATFORM_VALIDATION),
    MOTHER_SURNAME_REQUIRED("K000408", "Mother surname is required", ApiCodeLayer.PLATFORM_VALIDATION),
    INVALID_MOTHER_SURNAME_FORMAT("K000409", "Invalid mother surname format", ApiCodeLayer.PLATFORM_VALIDATION),
    OCR_SERVICE_ERROR("K000120", "OCR service error", ApiCodeLayer.PLATFORM_VALIDATION),
    OCR_IMAGE_INVALID("K000121", "OCR image invalid", ApiCodeLayer.PLATFORM_VALIDATION),
    OCR_NO_RESULT("K000122", "OCR no result", ApiCodeLayer.PLATFORM_VALIDATION),
    OCR_LIVENESS_FAILED("K000123", "OCR liveness detection failed", ApiCodeLayer.PLATFORM_VALIDATION),
    OCR_SESSION_INVALID("K000124", "OCR session invalid or expired", ApiCodeLayer.PLATFORM_VALIDATION),
    OCR_FACE_RECOGNITION_FAILED("K000125", "OCR face recognition failed", ApiCodeLayer.PLATFORM_VALIDATION),

    UPSTREAM_APPLICATION_NOT_FOUND("L000010", "Application not found", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INVALID_REQUEST_PARAMETERS("L000001", "Invalid request parameters rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INVALID_EDUCATION_DEGREE("L000056", "Invalid education degree rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INDUSTRY_REQUIRED("L000063", "Industry is required by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INCOME_REQUIRED("L000069", "Income is required by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INVALID_INCOME_FORMAT("L000078", "Invalid income format rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INVALID_EMAIL("L000075", "Invalid email rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_MOTHER_SURNAME_REQUIRED("L000408", "Mother surname is required by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INVALID_MOTHER_SURNAME_FORMAT("L000409", "Invalid mother surname format rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_DUPLICATE_SUBMISSION_IN_PROGRESS("L000144", "Duplicate submission in progress at lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_FACE_RECOGNITION_FAILED("L000052", "Face recognition failed rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INVALID_EKTP_FORMAT("L000053", "Invalid EKTP format rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_INVALID_OCR_RAW_DETAIL("L000445", "Invalid OCR raw detail rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_IDENTITY_NAME_REQUIRED("L000455", "Identity name required by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_IDENTITY_NAME_TOO_LONG("L000456", "Identity name too long rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    CREDIT_LIMIT_NOT_AVAILABLE("L000124", "Credit limit not available", ApiCodeLayer.UPSTREAM_BUSINESS),
    LENDER_LOAN_AMOUNT_REJECTED("L000145", "Loan amount rejected by lender", ApiCodeLayer.UPSTREAM_BUSINESS),
    BANK_CARD_VA_NOT_ALLOWED("L000104", "Virtual account card not allowed", ApiCodeLayer.UPSTREAM_BUSINESS),
    BANK_CARD_VERIFICATION_FAILED("L000321", "Bank card verification failed", ApiCodeLayer.UPSTREAM_BUSINESS),
    BANK_CARD_ALREADY_BOUND("L000339", "Bank card already bound", ApiCodeLayer.UPSTREAM_BUSINESS),
    BANK_CARD_NOT_FOUND("K000340", "Bank card not found", ApiCodeLayer.PLATFORM_VALIDATION),
    BANK_CARD_DEFAULT_CANNOT_DELETE("K000341", "Default bank card cannot be deleted", ApiCodeLayer.PLATFORM_VALIDATION),
    BANK_CARD_MAX_LIMIT_REACHED(
            "K000342",
            "Jumlah kartu bank yang dapat ditambahkan sudah maksimal",
            ApiCodeLayer.PLATFORM_VALIDATION
    ),
    BANK_CARD_LIST_ACCESS_DENIED(
            "K000343",
            "Status saat ini tidak memungkinkan modifikasi, silakan coba lagi nanti",
            ApiCodeLayer.PLATFORM_VALIDATION
    ),

    QUOTE_SNAPSHOT_EXPIRED("B000003", "Quote snapshot expired", ApiCodeLayer.PLATFORM_ORCHESTRATION),
    QUOTE_SNAPSHOT_MISMATCH("B000004", "Quote snapshot mismatch", ApiCodeLayer.PLATFORM_ORCHESTRATION),

    SERVICE_UNAVAILABLE("999998", "Service unavailable", ApiCodeLayer.SYSTEM),
    INTERNAL_SERVER_ERROR("999999", "Internal server error", ApiCodeLayer.SYSTEM);

    private static final Map<String, ApiCode> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ApiCode::code, Function.identity()));

    private final String code;
    private final String message;
    private final ApiCodeLayer layer;

    ApiCode(String code, String message, ApiCodeLayer layer) {
        this.code = code;
        this.message = message;
        this.layer = layer;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public ApiCodeLayer layer() {
        return layer;
    }

    public boolean success() {
        return this == SUCCESS;
    }

    public static ApiCode fromPublicCode(String code) {
        if (code != null && code.startsWith("A")) {
            throw new IllegalArgumentException("Raw upstream codes are not public API codes");
        }
        ApiCode apiCode = BY_CODE.get(code);
        if (apiCode == null) {
            throw new IllegalArgumentException("Unknown public API code: " + code);
        }
        return apiCode;
    }
}
