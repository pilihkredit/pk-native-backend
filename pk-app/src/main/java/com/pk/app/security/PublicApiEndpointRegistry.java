package com.pk.app.security;

import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

@Component
public class PublicApiEndpointRegistry implements InitializingBean {
    private final RequestMappingHandlerMapping handlerMapping;
    private final List<PublicEndpoint> publicEndpoints = new ArrayList<>();

    public PublicApiEndpointRegistry(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping
    ) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void afterPropertiesSet() {
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            if (!isPublicApi(entry.getValue())) {
                continue;
            }
            RequestMappingInfo mappingInfo = entry.getKey();
            var methodsCondition = mappingInfo.getMethodsCondition();
            var httpMethods = methodsCondition.getMethods();
            if (httpMethods.isEmpty()) {
                registerPatterns(mappingInfo, null);
                continue;
            }
            httpMethods.forEach(method -> registerPatterns(mappingInfo, method.name()));
        }
    }

    public boolean isPublic(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String method = request.getMethod();
        return publicEndpoints.stream().anyMatch(endpoint -> endpoint.matches(method, servletPath));
    }

    List<PublicEndpoint> publicEndpoints() {
        return List.copyOf(publicEndpoints);
    }

    private void registerPatterns(RequestMappingInfo mappingInfo, String httpMethod) {
        if (mappingInfo.getPathPatternsCondition() != null) {
            for (PathPattern pattern : mappingInfo.getPathPatternsCondition().getPatterns()) {
                publicEndpoints.add(new PublicEndpoint(httpMethod, pattern.getPatternString()));
            }
        }
        if (mappingInfo.getPatternsCondition() != null) {
            for (String pattern : mappingInfo.getPatternsCondition().getPatterns()) {
                publicEndpoints.add(new PublicEndpoint(httpMethod, pattern));
            }
        }
    }

    private static boolean isPublicApi(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(PublicApi.class)
                || handlerMethod.getBeanType().isAnnotationPresent(PublicApi.class);
    }

    public record PublicEndpoint(String httpMethod, String pattern) {
        boolean matches(String requestMethod, String servletPath) {
            boolean methodMatches = httpMethod == null || httpMethod.equalsIgnoreCase(requestMethod);
            return methodMatches && pathMatches(servletPath);
        }

        private boolean pathMatches(String servletPath) {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                return servletPath.equals(prefix) || servletPath.startsWith(prefix + "/");
            }
            return servletPath.equals(pattern);
        }
    }
}
