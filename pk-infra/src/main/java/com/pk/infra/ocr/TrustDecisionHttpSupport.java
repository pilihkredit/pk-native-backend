package com.pk.infra.ocr;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class TrustDecisionHttpSupport {
    private TrustDecisionHttpSupport() {
    }

    public static URI authenticatedUri(String endpoint, String partnerCode, String partnerKey) {
        String separator = endpoint.contains("?") ? "&" : "?";
        return URI.create(endpoint + separator
                + "partner_code=" + encode(partnerCode)
                + "&partner_key=" + encode(partnerKey));
    }

    public static String withoutQuery(URI uri) {
        return URI.create(uri.getScheme() + "://" + uri.getAuthority() + uri.getPath()).toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
