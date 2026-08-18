package com.pk.adapter.apipartner;

import com.pk.core.profile.port.LenderBankCardPort;

public class FakeApiPartnerBankCardAdapter implements LenderBankCardPort {
    @Override
    public void deleteBankCard(DeleteBankCardCommand command) {
        // no-op for local/dev
    }

    @Override
    public void setDefaultBankCard(SetDefaultBankCardCommand command) {
        // no-op for local/dev
    }
}
