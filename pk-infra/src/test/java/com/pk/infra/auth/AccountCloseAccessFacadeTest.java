package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AccountCloseAccessFacadeTest {
    private static final Map<Integer, String[]> LIFECYCLE_COPY = Map.of(
            3, new String[] {
                    "Akun Anda belum dapat ditutup karena pengajuan Anda sedang dalam proses peninjauan.",
                    "Mohon tunggu hingga proses peninjauan selesai. Anda dapat mengajukan penutupan akun kembali setelah proses tersebut selesai."
            },
            5, new String[] {
                    "Akun Anda belum dapat ditutup karena pengajuan pinjaman Anda sedang diproses.",
                    "Mohon tunggu hingga proses pengajuan selesai. Anda dapat mengajukan penutupan akun kembali setelah proses tersebut selesai."
            },
            6, new String[] {
                    "Akun Anda belum dapat ditutup karena dana pinjaman Anda menunggu untuk dicairkan.",
                    "Mohon tunggu hingga dana selesai dicairkan. Anda dapat mengajukan penutupan akun kembali setelah dana diterima."
            },
            7, new String[] {
                    "Akun Anda belum dapat ditutup karena dana pinjaman Anda sedang dalam proses pencairan.",
                    "Mohon tunggu hingga dana selesai dicairkan. Anda dapat mengajukan penutupan akun kembali setelah dana diterima."
            }
    );

    @Test
    void allowsAccountCloseForACompletedLoanWithoutOutstandingLoans() {
        LenderUserStatusPort lenderUserStatusPort = mock(LenderUserStatusPort.class);
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(4, 0));

        var result = new AccountCloseAccessFacade(lenderUserStatusPort).checkAccess(principal(), device());

        assertThat(result.canClose()).isTrue();
        assertThat(result.prompt()).isNull();
        assertThat(result.reason()).isNull();
    }

    @Test
    void returnsLifecycleCopyForBlockedLifetimeStatuses() {
        for (var entry : LIFECYCLE_COPY.entrySet()) {
            LenderUserStatusPort lenderUserStatusPort = mock(LenderUserStatusPort.class);
            when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(entry.getKey(), 0));

            var result = new AccountCloseAccessFacade(lenderUserStatusPort).checkAccess(principal(), device());

            assertThat(result.canClose()).isFalse();
            assertThat(result.prompt()).isEqualTo(entry.getValue()[0]);
            assertThat(result.reason()).isEqualTo(entry.getValue()[1]);
        }
    }

    @Test
    void returnsOnLoanCopyWhenThereAreOutstandingLoans() {
        LenderUserStatusPort lenderUserStatusPort = mock(LenderUserStatusPort.class);
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(4, 1));

        var result = new AccountCloseAccessFacade(lenderUserStatusPort).checkAccess(principal(), device());

        assertThat(result.canClose()).isFalse();
        assertThat(result.prompt()).isEqualTo(
                "Akun Anda belum dapat ditutup karena masih terdapat tagihan yang belum lunas."
        );
        assertThat(result.reason()).isEqualTo(
                "Silakan lunasi seluruh tagihan Anda terlebih dahulu. Anda dapat mengajukan penutupan akun kembali setelah seluruh tagihan lunas."
        );
    }

    @Test
    void prefersLifecycleCopyWhenBothLifecycleAndOnLoanBlock() {
        LenderUserStatusPort lenderUserStatusPort = mock(LenderUserStatusPort.class);
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(5, 2));

        var result = new AccountCloseAccessFacade(lenderUserStatusPort).checkAccess(principal(), device());

        assertThat(result.canClose()).isFalse();
        assertThat(result.prompt()).isEqualTo(LIFECYCLE_COPY.get(5)[0]);
        assertThat(result.reason()).isEqualTo(LIFECYCLE_COPY.get(5)[1]);
    }

    private static AuthenticatedPrincipal principal() {
        return new AuthenticatedPrincipal(1L, "U1", "8123456789", 1L);
    }

    private static LenderDeviceContext device() {
        return new LenderDeviceContext("PKApp", "1.0.0", "com.example.pk", "device-1", "android");
    }

    private static LenderUserStatusPort.LenderUserStatusResult statusResult(Integer status, Integer onLoanCount) {
        return new LenderUserStatusPort.LenderUserStatusResult(
                "U1", "USR1", status, null, null, null, null, null, onLoanCount, null, null, null
        );
    }
}
