package com.pk.app.common.web;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception, HttpServletRequest request) {
        if (shouldLogApiException(exception)) {
            log.error(
                    "API failure traceId={} code={} method={} path={}",
                    RequestTrace.resolveTraceId(request),
                    exception.apiCode().code(),
                    request.getMethod(),
                    request.getRequestURI(),
                    exception
            );
        }
        HttpStatus status = resolveApiExceptionStatus(exception);
        return failure(exception.apiCode(), status, request);
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
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception, HttpServletRequest request) {
        return failure(ApiCode.INVALID_REQUEST_PARAMETERS, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownException(Exception exception, HttpServletRequest request) {
        if (isValidationCause(exception)) {
            return failure(ApiCode.INVALID_REQUEST_PARAMETERS, HttpStatus.BAD_REQUEST, request);
        }
        log.error(
                "Unhandled API exception traceId={} method={} path={}",
                RequestTrace.resolveTraceId(request),
                request.getMethod(),
                request.getRequestURI(),
                exception
        );
        return failure(ApiCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ApiResponse<Void>> failure(ApiCode apiCode, HttpStatus status, HttpServletRequest request) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(apiCode, RequestTrace.resolveTraceId(request)));
    }

    private boolean isValidationCause(Exception exception) {
        Throwable cause = exception.getCause();
        return cause instanceof ValidationException
                || cause instanceof BindException
                || cause instanceof MethodArgumentNotValidException
                || cause instanceof HandlerMethodValidationException
                || cause instanceof MethodArgumentTypeMismatchException
                || cause instanceof MissingServletRequestParameterException;
    }
}
