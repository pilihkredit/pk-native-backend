package com.pk.core.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint as publicly accessible without a Bearer access token.
 * Use only for pre-login flows ({@code /app/disclosure/*}, {@code /auth/mobile/check},
 * {@code /auth/otp/*}, {@code /auth/password/login}, {@code /auth/refresh},
 * {@code /agreement/*}) and session renewal via refresh token.
 * All post-login business APIs (bank/area dictionaries, profile, credit, etc.) must not use this.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicApi {
}
