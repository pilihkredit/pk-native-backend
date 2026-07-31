package com.pk.app.profile.dto.response;

public record ProfileMobileChangeResponse(
        boolean changed,
        String mobileNo
) {
}
