package com.pk.core.profile.port;

public interface LenderBankCardPort {
    void deleteBankCard(DeleteBankCardCommand command);

    record DeleteBankCardCommand(String partnerUserId, long bankCardId) {
    }
}
