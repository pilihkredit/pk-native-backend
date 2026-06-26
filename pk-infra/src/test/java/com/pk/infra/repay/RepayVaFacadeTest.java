package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.port.LenderRepayVaPort;
import com.pk.core.repay.port.RepayVaSnapshotRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepayVaFacadeTest {
    @Mock
    private LenderRepayVaPort lenderRepayVaPort;
    @Mock
    private RepayVaSnapshotRepository repayVaSnapshotRepository;

    private RepayVaFacade facade;

    @BeforeEach
    void setUp() {
        facade = new RepayVaFacade(lenderRepayVaPort, repayVaSnapshotRepository, new ObjectMapper());
    }

    @Test
    void listVasPersistsSnapshotsAndMapsStatus() {
        when(lenderRepayVaPort.listVas("U10001")).thenReturn(new LenderRepayVaPort.LenderRepayVaListResult(
                "U10001",
                "USR-1",
                va(true, false),
                List.of(va(true, false)),
                "{}"
        ));

        RepayVaFacade.VaListResult result = facade.listVas(1L, "U10001");

        assertThat(result.vaList()).hasSize(1);
        assertThat(result.vaList().getFirst().status()).isEqualTo("ACTIVE");
        assertThat(result.vaList().getFirst().bankChannel()).isEqualTo("BCA");
        verify(repayVaSnapshotRepository).replaceSnapshots(anyLong(), anyString(), any(), any(Instant.class));
    }

    @Test
    void setDefaultVaCallsLenderAndRefreshesSnapshots() {
        when(lenderRepayVaPort.listVas("U10001")).thenReturn(new LenderRepayVaPort.LenderRepayVaListResult(
                "U10001",
                "USR-1",
                va(true, false),
                List.of(va(true, false)),
                "{}"
        ));

        RepayVaFacade.VaDefaultResult result = facade.setDefaultVa(
                1L,
                "U10001",
                new RepayVaFacade.VaDefaultCommand("8801234567890", "BCA")
        );

        assertThat(result.vaNo()).isEqualTo("8801234567890");
        assertThat(result.defaultFlag()).isTrue();
        verify(lenderRepayVaPort).setDefaultVa(any());
        verify(repayVaSnapshotRepository).replaceSnapshots(anyLong(), anyString(), any(), any(Instant.class));
    }

    private static LenderRepayVa va(boolean defaultFlag, boolean disabled) {
        return new LenderRepayVa(
                "8801234567890",
                "BCA",
                "Bank Central Asia",
                1,
                null,
                List.of(),
                defaultFlag,
                disabled,
                true
        );
    }
}
