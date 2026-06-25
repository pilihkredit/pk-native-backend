package com.pk.app.bank.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pk.core.reference.BankReference;
import com.pk.infra.reference.BankReferenceFacade;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankApplicationServiceTest {
    @Mock
    private BankReferenceFacade bankReferenceFacade;

    @InjectMocks
    private BankApplicationService bankApplicationService;

    @Test
    void mapsBankReferencesToApiResponses() {
        when(bankReferenceFacade.listBanks()).thenReturn(List.of(
                new BankReference("BCA", "Bank Central Asia", 1, "https://example.com/bca.png")
        ));

        var responses = bankApplicationService.listBanks();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().bankCode()).isEqualTo("BCA");
        assertThat(responses.getFirst().bankName()).isEqualTo("Bank Central Asia");
        assertThat(responses.getFirst().bankType()).isEqualTo("1");
        assertThat(responses.getFirst().iconUrl()).isEqualTo("https://example.com/bca.png");
    }
}
