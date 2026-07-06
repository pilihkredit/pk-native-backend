package com.pk.infra.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.reference.BankReference;
import com.pk.core.reference.port.LenderBankPort;
import com.pk.core.reference.port.RefBankRepository;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankReferenceFacadeTest {
    private RefBankRepository refBankRepository;
    private LenderBankPort lenderBankPort;
    private BankReferenceFacade facade;

    @BeforeEach
    void setUp() {
        refBankRepository = org.mockito.Mockito.mock(RefBankRepository.class);
        lenderBankPort = org.mockito.Mockito.mock(LenderBankPort.class);
        ReferenceProperties properties = new ReferenceProperties();
        properties.setBankCacheTtl(Duration.ofHours(24));
        facade = new BankReferenceFacade(refBankRepository, lenderBankPort, properties);
    }

    @Test
    void listBanksAlwaysReturnsLenderOrderAndRefreshesCache() {
        List<BankReference> lenderBanks = List.of(
                new BankReference("MANDIRI", "Bank Mandiri", null, null),
                new BankReference("BCA", "Bank Central Asia", null, null)
        );
        when(lenderBankPort.listBanks()).thenReturn(lenderBanks);

        List<BankReference> result = facade.listBanks();

        assertThat(result).containsExactlyElementsOf(lenderBanks);
        verify(lenderBankPort).listBanks();
        verify(refBankRepository).replaceAll(eq(lenderBanks), any());
    }

    @Test
    void syncsFromLenderWhenCacheEmpty() {
        when(lenderBankPort.listBanks()).thenReturn(sampleBanks());

        List<BankReference> result = facade.listBanks();

        assertThat(result).containsExactlyElementsOf(sampleBanks());
        verify(refBankRepository).replaceAll(eq(sampleBanks()), any());
    }

    @Test
    void throwsServiceUnavailableWhenLenderReturnsEmpty() {
        when(lenderBankPort.listBanks()).thenReturn(List.of());

        assertThatThrownBy(() -> facade.listBanks())
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);
    }

    private static List<BankReference> sampleBanks() {
        return List.of(
                new BankReference("BCA", "Bank Central Asia", null, null),
                new BankReference("MANDIRI", "Bank Mandiri", null, null)
        );
    }
}
