package com.pk.app.common.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Injects active lender provider identity into every {@link ApiResponse} body.
 */
@RestControllerAdvice
public class ApiResponseLenderProviderAdvice implements ResponseBodyAdvice<Object> {
    private final ActiveLenderProvider activeLenderProvider;

    public ApiResponseLenderProviderAdvice(ActiveLenderProvider activeLenderProvider) {
        this.activeLenderProvider = activeLenderProvider;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body instanceof ApiResponse<?> apiResponse) {
            return activeLenderProvider.enrich(apiResponse);
        }
        return body;
    }
}
