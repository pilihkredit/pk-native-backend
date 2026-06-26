package com.pk.adapter.pendanaan;

import com.pk.core.reference.BankReference;
import com.pk.core.reference.port.LenderBankPort;
import java.util.List;

public class FakePendanaanBankAdapter implements LenderBankPort {
    @Override
    public List<BankReference> listBanks() {
        return List.of(
                new BankReference("BCA", "Bank Central Asia", null, null),
                new BankReference("MANDIRI", "Bank Mandiri", null, null),
                new BankReference("BNI", "Bank Negara Indonesia", null, null),
                new BankReference("BRI", "Bank Rakyat Indonesia", null, null)
        );
    }
}
