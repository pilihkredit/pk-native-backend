package com.pk.app.common.web;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.logging.StructuredLogWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;

@RestControllerAdvice
public class GlobalApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    private final StructuredLogWriter structuredLogWriter;

    public GlobalApiExceptionHandler(StructuredLogWriter structuredLogWriter) {
        this.structuredLogWriter = structuredLogWriter;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception, HttpServletRequest request) {
        ValidationFailureLogger.logApiException(log, exception, request);
        if (shouldLogApiException(exception)) {
            structuredLogWriter.logError(
                    "api.error",
                    RequestTrace.resolveTraceId(request),
                    request.getRequestURI(),
                    request.getMethod(),
                    Map.of("code", exception.apiCode().code()),
                    exception
            );
        }
        HttpStatus status = resolveApiExceptionStatus(exception);
        return failure(
                exception.apiCode(),
                ValidationFailureMessages.forApiException(exception),
                status,
                request
        );
    }

    private static boolean shouldLogApiException(ApiException exception) {
        return exception.apiCode() == ApiCode.INTERNAL_SERVER_ERROR
                || exception.apiCode() == ApiCode.SERVICE_UNAVAILABLE;
    }

    private HttpStatus resolveApiExceptionStatus(ApiException exception) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class);
        if (responseStatus != null && responseStatus.code() != HttpStatus.INTERNAL_SERVER_ERROR) {
            return responseStatus.code();
        }
        return switch (exception.apiCode()) {
            case UNAUTHORIZED_REQUEST -> HttpStatus.UNAUTHORIZED;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    @ExceptionHandler({
            BindException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception, HttpServletRequest request) {
        ValidationFailureLogger.logValidationException(log, exception, request);
        return failure(
                ApiCode.INVALID_REQUEST_PARAMETERS,
                ValidationFailureMessages.forValidationException(exception),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownException(Exception exception, HttpServletRequest request) {
        if (isValidationCause(exception)) {
            Exception validationException = (Exception) exception.getCause();
            ValidationFailureLogger.logValidationException(log, validationException, request);
            return failure(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    ValidationFailureMessages.forValidationException(validationException),
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }
        structuredLogWriter.logError(
                "api.error",
                RequestTrace.resolveTraceId(request),
                request.getRequestURI(),
                request.getMethod(),
                null,
                exception
        );
        return failure(
                ApiCode.INTERNAL_SERVER_ERROR,
                ApiCode.INTERNAL_SERVER_ERROR.message(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    private ResponseEntity<ApiResponse<Void>> failure(
            ApiCode apiCode,
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(apiCode, message, RequestTrace.resolveTraceId(request)));
    }

    private boolean isValidationCause(Exception exception) {
        Throwable cause = exception.getCause();
        return cause instanceof ValidationException
                || cause instanceof BindException
                || cause instanceof MethodArgumentNotValidException
                || cause instanceof HandlerMethodValidationException
                || cause instanceof MethodArgumentTypeMismatchException
                || cause instanceof MissingServletRequestParameterException
                || cause instanceof HttpMessageNotReadableException;
    }
}
