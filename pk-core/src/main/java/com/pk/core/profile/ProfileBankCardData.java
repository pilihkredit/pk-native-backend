package com.pk.core.profile;

public record ProfileBankCardData(
        long profileId,
        String mobileNo,
        String bankCode,
        EncryptedField cardNumber,
        String cardNoHash,
        String verifyStatus,
        String verifyErrorCode,
        String moduleStatus,
        String lastRequestId,
        String lastLenderRequestJson,
        String lastLenderResponseJson
) {
}
