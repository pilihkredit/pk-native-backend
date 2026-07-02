package com.pk.app.common.web;

import com.pk.core.api.ApiCodeLayer;
import com.pk.core.api.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.method.ParameterValidationResult;

/**
 * Builds client-facing validation messages for API {@code msg} fields.
 */
public final class ValidationFailureMessages {
    private ValidationFailureMessages() {
    }

    public static String forApiException(ApiException exception) {
        if (usesPlatformOwnedMessageOnly(exception)) {
            return exception.apiCode().message();
        }
        String detail = exception.detail();
        if (detail != null && !detail.isBlank()) {
            return detail;
        }
        return exception.apiCode().message();
    }

    /**
     * Lender/upstream failures use PK-maintained English {@link ApiCode#message()} only.
     * Platform validation may still expose field-level {@link ApiException#detail()}.
     */
    private static boolean usesPlatformOwnedMessageOnly(ApiException exception) {
        ApiCodeLayer layer = exception.apiCode().layer();
        return layer == ApiCodeLayer.UPSTREAM_BUSINESS || layer == ApiCodeLayer.SYSTEM;
    }

    public static String forValidationException(Exception exception) {
        return describeValidationException(exception);
    }

    static String describeValidationException(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            return formatFieldErrors(methodArgumentNotValidException.getBindingResult().getFieldErrors());
        }
        if (exception instanceof BindException bindException) {
            return formatFieldErrors(bindException.getBindingResult().getFieldErrors());
        }
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations().stream()
                    .map(ValidationFailureMessages::formatConstraintViolation)
                    .collect(Collectors.joining("; "));
        }
        if (exception instanceof HandlerMethodValidationException handlerMethodValidationException) {
            return handlerMethodValidationException.getAllValidationResults().stream()
                    .map(ValidationFailureMessages::formatParameterValidationResult)
                    .filter(message -> !message.isBlank())
                    .collect(Collectors.joining("; "));
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatchException) {
            String requiredType = mismatchException.getRequiredType() == null
                    ? "unknown"
                    : mismatchException.getRequiredType().getSimpleName();
            return mismatchException.getName()
                    + ": expected type "
                    + requiredType
                    + ", value="
                    + mismatchException.getValue();
        }
        if (exception instanceof MissingServletRequestParameterException missingParameterException) {
            return missingParameterException.getParameterName() + ": is required";
        }
        if (exception instanceof HttpMessageNotReadableException notReadableException) {
            Throwable cause = notReadableException.getMostSpecificCause();
            String message = cause == null ? notReadableException.getMessage() : cause.getMessage();
            return message == null || message.isBlank() ? "request body is invalid" : message;
        }
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "Invalid request parameters" : message;
    }

    private static String formatParameterValidationResult(ParameterValidationResult result) {
        String fieldName = resolveParameterFieldName(result);
        return result.getResolvableErrors().stream()
                .map(error -> formatNamedValidationError(fieldName, error))
                .collect(Collectors.joining("; "));
    }

    private static String resolveParameterFieldName(ParameterValidationResult result) {
        String parameterName = result.getMethodParameter().getParameterName();
        String fieldName = parameterName == null || parameterName.isBlank() ? "parameter" : parameterName;
        if (result.getContainerKey() != null) {
            return fieldName + "." + result.getContainerKey();
        }
        if (result.getContainerIndex() != null) {
            return fieldName + "[" + result.getContainerIndex() + "]";
        }
        return fieldName;
    }

    private static String formatNamedValidationError(String fieldName, org.springframework.context.MessageSourceResolvable error) {
        String message = resolveErrorMessage(error);
        if (fieldName == null || fieldName.isBlank()) {
            return message;
        }
        return fieldName + ": " + message;
    }

    private static String resolveErrorMessage(org.springframework.context.MessageSourceResolvable error) {
        if (error.getDefaultMessage() != null && !error.getDefaultMessage().isBlank()) {
            return error.getDefaultMessage();
        }
        if (error instanceof DefaultMessageSourceResolvable resolvable
                && resolvable.getCodes() != null
                && resolvable.getCodes().length > 0) {
            return resolvable.getCodes()[0];
        }
        return error.toString();
    }

    private static String formatFieldErrors(java.util.List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> error.getField() + ": " + resolveFieldErrorMessage(error))
                .collect(Collectors.joining("; "));
    }

    private static String resolveFieldErrorMessage(FieldError error) {
        if (error.getDefaultMessage() != null && !error.getDefaultMessage().isBlank()) {
            return error.getDefaultMessage();
        }
        return "is invalid";
    }

    private static String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
