package com.pk.infra.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Exchanges a Firebase service-account JSON for a short-lived Google OAuth access token. */
public class GoogleServiceAccountTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(GoogleServiceAccountTokenProvider.class);
    private static final String TOKEN_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(55);

    private final FcmProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AtomicReference<CachedToken> cached = new AtomicReference<>();

    public GoogleServiceAccountTokenProvider(FcmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String accessToken() {
        CachedToken current = cached.get();
        Instant now = Instant.now();
        if (current != null && current.expiresAt().isAfter(now.plusSeconds(60))) {
            return current.token();
        }
        synchronized (this) {
            current = cached.get();
            now = Instant.now();
            if (current != null && current.expiresAt().isAfter(now.plusSeconds(60))) {
                return current.token();
            }
            CachedToken refreshed = fetchAccessToken();
            cached.set(refreshed);
            return refreshed.token();
        }
    }

    private CachedToken fetchAccessToken() {
        try {
            JsonNode credentials = loadCredentials();
            String clientEmail = text(credentials, "client_email");
            String privateKeyPem = normalizePrivateKeyPem(text(credentials, "private_key"));
            String tokenUri = text(credentials, "token_uri");
            if (clientEmail.isBlank() || privateKeyPem.isBlank() || tokenUri.isBlank()) {
                throw new ApiException(
                        ApiCode.SERVICE_UNAVAILABLE,
                        "FCM credentials incomplete: need client_email, private_key, token_uri"
                );
            }
            String assertion = signJwt(clientEmail, privateKeyPem, tokenUri);
            String body = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", StandardCharsets.UTF_8)
                    + "&assertion=" + URLEncoder.encode(assertion, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUri))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body() == null ? "" : response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Google OAuth token exchange failed status={} body={}", response.statusCode(), truncate(responseBody));
                throw new ApiException(
                        ApiCode.SERVICE_UNAVAILABLE,
                        "Google OAuth token exchange failed HTTP " + response.statusCode() + ": " + truncate(responseBody)
                );
            }
            JsonNode json;
            try {
                json = objectMapper.readTree(responseBody);
            } catch (IOException parseException) {
                throw new ApiException(
                        ApiCode.SERVICE_UNAVAILABLE,
                        "Google OAuth response JSON parse failed (len=" + responseBody.length()
                                + "): " + rootMessage(parseException)
                );
            }
            String accessToken = text(json, "access_token");
            if (accessToken.isBlank()) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, "Google OAuth response missing access_token");
            }
            return new CachedToken(accessToken, Instant.now().plus(TOKEN_TTL));
        } catch (ApiException exception) {
            throw exception;
        } catch (java.net.http.HttpTimeoutException exception) {
            log.warn("Google OAuth token exchange timed out", exception);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, "Google OAuth token exchange timed out");
        } catch (IOException exception) {
            log.warn("Google OAuth token exchange I/O failed", exception);
            throw new ApiException(
                    ApiCode.SERVICE_UNAVAILABLE,
                    "Google OAuth token exchange I/O failed: " + rootMessage(exception)
            );
        } catch (Exception exception) {
            log.warn("Google OAuth token exchange failed", exception);
            throw new ApiException(
                    ApiCode.SERVICE_UNAVAILABLE,
                    "Google OAuth token exchange failed: " + rootMessage(exception)
            );
        }
    }

    private JsonNode loadCredentials() throws IOException {
        // Prefer path, then base64, then raw JSON (raw JSON is often truncated in deploy env/YAML).
        if (properties.credentialsPath() != null && !properties.credentialsPath().isBlank()) {
            Path path = Path.of(properties.credentialsPath().trim());
            try {
                return objectMapper.readTree(Files.readString(path));
            } catch (IOException exception) {
                throw new ApiException(
                        ApiCode.SERVICE_UNAVAILABLE,
                        "FCM credentials-path JSON parse failed (" + path + "): " + rootMessage(exception)
                );
            }
        }
        if (properties.credentialsJsonBase64() != null && !properties.credentialsJsonBase64().isBlank()) {
            String compact = properties.credentialsJsonBase64().replaceAll("\\s+", "");
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(compact);
            } catch (IllegalArgumentException exception) {
                throw new ApiException(
                        ApiCode.SERVICE_UNAVAILABLE,
                        "FCM credentials-json-base64 is not valid Base64 (len=" + compact.length() + ")"
                );
            }
            String json = new String(decoded, StandardCharsets.UTF_8).trim();
            try {
                return objectMapper.readTree(json);
            } catch (IOException exception) {
                throw new ApiException(
                        ApiCode.SERVICE_UNAVAILABLE,
                        "FCM credentials-json-base64 decoded JSON parse failed (decodedLen="
                                + json.length() + ", prefix=" + safePrefix(json) + "): "
                                + rootMessage(exception)
                );
            }
        }
        if (properties.credentialsJson() != null && !properties.credentialsJson().isBlank()) {
            String raw = properties.credentialsJson().trim();
            if ((raw.startsWith("'") && raw.endsWith("'")) || (raw.startsWith("\"") && raw.endsWith("\""))) {
                raw = raw.substring(1, raw.length() - 1);
            }
            try {
                return objectMapper.readTree(raw);
            } catch (IOException exception) {
                throw new ApiException(
                        ApiCode.SERVICE_UNAVAILABLE,
                        "FCM credentials-json parse failed (len=" + raw.length()
                                + ", prefix=" + safePrefix(raw)
                                + "). Prefer PK_FCM_CREDENTIALS_JSON_BASE64 or PK_FCM_CREDENTIALS_PATH: "
                                + rootMessage(exception)
                );
            }
        }
        throw new ApiException(
                ApiCode.SERVICE_UNAVAILABLE,
                "FCM credentials missing: set PK_FCM_CREDENTIALS_PATH, PK_FCM_CREDENTIALS_JSON_BASE64, or PK_FCM_CREDENTIALS_JSON"
        );
    }

    private static String signJwt(String clientEmail, String privateKeyPem, String tokenUri)
            throws JOSEException, java.security.GeneralSecurityException {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(clientEmail)
                .subject(clientEmail)
                .audience(tokenUri)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(Duration.ofHours(1))))
                .claim("scope", TOKEN_SCOPE)
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(parsePrivateKey(privateKeyPem)));
        return jwt.serialize();
    }

    private static String normalizePrivateKeyPem(String pem) {
        if (pem == null || pem.isBlank()) {
            return "";
        }
        // Fix env values where "\\n" survived as literal backslash-n instead of newlines.
        if (pem.contains("\\n") && !pem.contains("\n")) {
            return pem.replace("\\n", "\n");
        }
        return pem;
    }

    private static RSAPrivateKey parsePrivateKey(String pem) throws java.security.GeneralSecurityException {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? "" : value.asText("").trim();
    }

    private static String truncate(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().replaceAll("\\s+", " ");
        return trimmed.length() <= 300 ? trimmed : trimmed.substring(0, 300) + "...";
    }

    private static String safePrefix(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }
        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() <= 24 ? compact : compact.substring(0, 24) + "...";
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            return current.getClass().getSimpleName();
        }
        return truncate(message);
    }

    private record CachedToken(String token, Instant expiresAt) {
    }
}
