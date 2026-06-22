package com.pk.app.api;

import com.pk.app.web.RequestSupport;
import com.pk.core.error.AppBusinessException;
import com.pk.core.error.AppErrorCodes;
import com.pk.core.error.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppBusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusiness(AppBusinessException ex, HttpServletRequest request) {
        return new ApiResponse<>(ex.code(), ex.msg(), null, RequestSupport.traceId(request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String code = AppErrorCodes.INVALID_REQUEST;
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null && "deviceNo".equals(fieldError.getField())) {
            code = AppErrorCodes.DEVICE_NO_REQUIRED;
        }
        return new ApiResponse<>(code, ErrorMessages.forCode(code), null, RequestSupport.traceId(request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return new ApiResponse<>(
                AppErrorCodes.INVALID_REQUEST,
                ErrorMessages.forCode(AppErrorCodes.INVALID_REQUEST),
                null,
                RequestSupport.traceId(request)
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleUnexpected(Exception ex, HttpServletRequest request) {
        return new ApiResponse<>(
                AppErrorCodes.INTERNAL_ERROR,
                ErrorMessages.forCode(AppErrorCodes.INTERNAL_ERROR),
                null,
                RequestSupport.traceId(request)
        );
    }
}
