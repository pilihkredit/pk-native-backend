package com.pk.infra.profile.repository;
public record ProfilePersonalRow(long userId,
int educationDegree, int industry, String income,
        String motherSurnameCiphertext, byte[] motherSurnameNonce, byte[] motherSurnameTag,
        String userEmail, String moduleStatus, String lastRequestId,
        Long externalInteractionId) {}
