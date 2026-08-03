package com.pk.core.profile.port;

public interface LenderBankCardPort {
    void deleteBankCard(DeleteBankCardCommand command);

    void setDefaultBankCard(SetDefaultBankCardCommand command);

    record DeleteBankCardCommand(String partnerUserId, long bankCardId) {
    }

    record SetDefaultBankCardCommand(String partnerUserId, long bankCardId) {
    }
}
