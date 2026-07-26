package com.pk.core.profile;

public record ProfileBankCardData(
        Long id,
        long userId,
String bankCode,
        EncryptedField cardNumber,
        String cardNoHash,
        String verifyStatus,
        String verifyErrorCode,
        boolean defaultFlag,
        boolean deletedFlag,
        String moduleStatus,
        String lastRequestId,
        Long externalInteractionId
) {
}
