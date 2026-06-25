package com.pk.app.callback.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CallbackOAuthTokenRequest(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank @JsonProperty("grantType") String grantType
) {
}
