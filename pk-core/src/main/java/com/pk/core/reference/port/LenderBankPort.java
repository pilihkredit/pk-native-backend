package com.pk.core.reference.port;

import com.pk.core.reference.BankReference;
import java.util.List;

public interface LenderBankPort {
    List<BankReference> listBanks();
}
