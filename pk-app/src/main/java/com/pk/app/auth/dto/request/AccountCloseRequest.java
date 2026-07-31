package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Optional body for account closure.
 *
 * @param reason optional close reason (audit/logging only for now)
 */
public record AccountCloseRequest(
        @Size(max = 500) String reason
) {
}
