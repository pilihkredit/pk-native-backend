package com.pk.app.common.web;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiCodeLayer;
import com.pk.core.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
                exception.apiCode().message(),
                resolveReason(exception),
                request.getMethod(),
                request.getRequestURI()
        );
    }

    static void logValidationException(Logger log, Exception exception, HttpServletRequest request) {
        log.warn(
                "API request validation failed traceId={} reason={} method={} path={}",
                RequestTrace.resolveTraceId(request),
                describeValidationException(exception),
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
                 PASSWORD_ACCOUNT_LOCKED,
                 DUPLICATE_SUBMISSION_IN_PROGRESS -> false;
            default -> {
                ApiCodeLayer layer = exception.apiCode().layer();
                yield layer == ApiCodeLayer.PLATFORM_VALIDATION
                        || layer == ApiCodeLayer.PLATFORM_ORCHESTRATION;
            }
        };
    }

    private static String resolveReason(ApiException exception) {
        if (exception.detail() != null) {
            return exception.detail();
        }
        String message = exception.getMessage();
        if (message != null && !message.equals(exception.apiCode().message())) {
            return message;
        }
        return exception.apiCode().message();
    }

    private static String describeValidationException(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            return formatFieldErrors(methodArgumentNotValidException.getBindingResult().getFieldErrors());
        }
        if (exception instanceof BindException bindException) {
            return formatFieldErrors(bindException.getBindingResult().getFieldErrors());
        }
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations().stream()
                    .map(ValidationFailureLogger::formatConstraintViolation)
                    .collect(Collectors.joining("; "));
        }
        if (exception instanceof HandlerMethodValidationException handlerMethodValidationException) {
            return handlerMethodValidationException.getAllValidationResults().stream()
                    .flatMap(result -> result.getResolvableErrors().stream())
                    .map(error -> error.getDefaultMessage() == null ? error.toString() : error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatchException) {
            return "parameter=" + mismatchException.getName()
                    + ", value=" + mismatchException.getValue()
                    + ", requiredType=" + mismatchException.getRequiredType();
        }
        if (exception instanceof MissingServletRequestParameterException missingParameterException) {
            return "missing parameter: " + missingParameterException.getParameterName();
        }
        if (exception instanceof HttpMessageNotReadableException notReadableException) {
            Throwable cause = notReadableException.getMostSpecificCause();
            return cause == null ? notReadableException.getMessage() : cause.getMessage();
        }
        return exception.getMessage();
    }

    private static String formatFieldErrors(java.util.List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

    private static String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
