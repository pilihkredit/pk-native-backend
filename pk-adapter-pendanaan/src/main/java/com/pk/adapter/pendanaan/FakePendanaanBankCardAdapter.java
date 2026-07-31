package com.pk.adapter.pendanaan;

import com.pk.core.profile.port.LenderBankCardPort;

public class FakePendanaanBankCardAdapter implements LenderBankCardPort {
    @Override
    public void deleteBankCard(DeleteBankCardCommand command) {
        // no-op for local/dev
    }
}
