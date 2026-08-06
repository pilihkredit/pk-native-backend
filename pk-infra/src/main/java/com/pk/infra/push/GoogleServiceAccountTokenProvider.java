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

/** Exchanges a Firebase service-account JSON for a short-lived Google OAuth access token. */
public class GoogleServiceAccountTokenProvider {
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
            String privateKeyPem = text(credentials, "private_key");
            String tokenUri = text(credentials, "token_uri");
            if (clientEmail.isBlank() || privateKeyPem.isBlank() || tokenUri.isBlank()) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
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
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
            }
            JsonNode json = objectMapper.readTree(response.body());
            String accessToken = text(json, "access_token");
            if (accessToken.isBlank()) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
            }
            return new CachedToken(accessToken, Instant.now().plus(TOKEN_TTL));
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private JsonNode loadCredentials() throws IOException {
        if (properties.credentialsJson() != null && !properties.credentialsJson().isBlank()) {
            return objectMapper.readTree(properties.credentialsJson());
        }
        Path path = Path.of(properties.credentialsPath().trim());
        return objectMapper.readTree(Files.readString(path));
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

    private record CachedToken(String token, Instant expiresAt) {
    }
}
