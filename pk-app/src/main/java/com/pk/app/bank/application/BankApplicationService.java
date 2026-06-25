package com.pk.app.bank.application;

import com.pk.app.bank.dto.response.BankListItemResponse;
import com.pk.core.reference.BankReference;
import com.pk.infra.reference.BankReferenceFacade;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BankApplicationService {
    private final BankReferenceFacade bankReferenceFacade;

    public BankApplicationService(BankReferenceFacade bankReferenceFacade) {
        this.bankReferenceFacade = bankReferenceFacade;
    }

    public List<BankListItemResponse> listBanks() {
        return bankReferenceFacade.listBanks().stream()
                .map(BankApplicationService::toResponse)
                .toList();
    }

    private static BankListItemResponse toResponse(BankReference bank) {
        String bankType = bank.bankType() == null ? null : String.valueOf(bank.bankType());
        return new BankListItemResponse(
                bank.bankCode(),
                bank.bankName(),
                bankType,
                bank.iconUrl()
        );
    }
}
