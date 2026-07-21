package com.pk.app.common.web;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiCodeLayer;
import com.pk.core.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;

final class ValidationFailureLogger {
    private ValidationFailureLogger() {
    }

    static void logApiException(Logger log, ApiException exception, HttpServletRequest request) {
        if (!isParameterValidationFailure(exception)) {
            return;
        }
        log.warn(
                "API parameter validation failed traceId={} code={} msg={} reason={} method={} path={}",
                RequestTrace.resolveTraceId(request),
                exception.apiCode().code(),
                ValidationFailureMessages.forApiException(exception),
                resolveReason(exception),
                request.getMethod(),
                request.getRequestURI()
        );
    }

    static void logValidationException(Logger log, Exception exception, HttpServletRequest request) {
        log.warn(
                "API request validation failed traceId={} reason={} method={} path={}",
                RequestTrace.resolveTraceId(request),
                ValidationFailureMessages.describeValidationException(exception),
                request.getMethod(),
                request.getRequestURI()
        );
    }

    private static boolean isParameterValidationFailure(ApiException exception) {
        return switch (exception.apiCode()) {
            case UNAUTHORIZED_REQUEST,
                 TOO_MANY_REQUESTS,
                 INVALID_OR_EXPIRED_VERIFICATION_CODE,
                 OTP_TOKEN_MISMATCH,
                 PASSWORD_ALREADY_SET,
                 PASSWORD_NOT_SET,
                 INVALID_MOBILE_OR_PASSWORD,
                 DUPLICATE_SUBMISSION_IN_PROGRESS -> false;
            default -> {
                ApiCodeLayer layer = exception.apiCode().layer();
                yield layer == ApiCodeLayer.PLATFORM_VALIDATION
                        || layer == ApiCodeLayer.PLATFORM_ORCHESTRATION;
            }
        };
    }

    private static String resolveReason(ApiException exception) {
        return ValidationFailureMessages.forApiException(exception);
    }
}
