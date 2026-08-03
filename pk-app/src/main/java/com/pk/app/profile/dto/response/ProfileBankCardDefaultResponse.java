package com.pk.app.profile.dto.response;

public record ProfileBankCardDefaultResponse(
        String requestId,
        long bankCardId,
        boolean defaultFlag
) {
}
