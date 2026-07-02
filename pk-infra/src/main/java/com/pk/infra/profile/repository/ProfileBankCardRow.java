package com.pk.infra.profile.repository;

public record ProfileBankCardRow(
        long profileId, String mobileNo, String bankCode,
        String cardNoCiphertext, byte[] cardNoNonce, byte[] cardNoTag, String cardNoHash,
        String verifyStatus, String verifyErrorCode, String moduleStatus, String lastRequestId,
        String lastLenderRequestJson, String lastLenderResponseJson
) {}
