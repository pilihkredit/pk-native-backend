package com.pk.core.profile;

public record ProfileContactData(
        String mobileNo,
        int sortNo,
        int relationship,
        String contactName,
        String contactMobile
) {
}
