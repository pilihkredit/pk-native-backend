package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Mirrors lender {@code POST /api/open/v1/user/info/query} request body.
 */
public record ProfileInfoQueryRequest(
        @NotBlank @Size(max = 64) String partnerUserId,
        List<@NotBlank String> modules
) {
}
