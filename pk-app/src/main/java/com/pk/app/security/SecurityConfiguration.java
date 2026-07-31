package com.pk.app.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    /**
     * Prevent Spring Boot from also registering {@link JwtAuthenticationFilter} as a servlet filter.
     * It must run only inside the Security filter chain (see {@link #securityFilterChain}).
     */
    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @DependsOn("publicApiEndpointRegistry")
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            PublicApiEndpointRegistry publicApiEndpointRegistry
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(
                                    "/actuator/health",
                                    "/actuator/info",
                                    "/actuator/prometheus")
                            .permitAll();
                    publicApiEndpointRegistry.publicEndpoints().forEach(endpoint -> {
                        if (endpoint.httpMethod() == null) {
                            authorize.requestMatchers(endpoint.pattern()).permitAll();
                        } else {
                            authorize.requestMatchers(HttpMethod.valueOf(endpoint.httpMethod()), endpoint.pattern()).permitAll();
                        }
                    });
                    authorize.anyRequest().authenticated();
                })
                .exceptionHandling(handler -> handler.authenticationEntryPoint(authenticationEntryPoint))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .anonymous(Customizer.withDefaults())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
