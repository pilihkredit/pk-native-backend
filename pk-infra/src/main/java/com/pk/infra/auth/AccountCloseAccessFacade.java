package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.Map;
import java.util.Set;

/** Real-time lender gate for account closure. */
public class AccountCloseAccessFacade {
    private static final Set<Integer> BLOCKED_LIFE_TIME_STATUSES = Set.of(3, 5, 6, 7);

    private static final Map<Integer, BlockCopy> LIFECYCLE_COPY = Map.of(
            3, new BlockCopy(
                    "Akun Anda belum dapat ditutup karena pengajuan Anda sedang dalam proses peninjauan.",
                    "Mohon tunggu hingga proses peninjauan selesai. Anda dapat mengajukan penutupan akun kembali setelah proses tersebut selesai."
            ),
            5, new BlockCopy(
                    "Akun Anda belum dapat ditutup karena pengajuan pinjaman Anda sedang diproses.",
                    "Mohon tunggu hingga proses pengajuan selesai. Anda dapat mengajukan penutupan akun kembali setelah proses tersebut selesai."
            ),
            6, new BlockCopy(
                    "Akun Anda belum dapat ditutup karena dana pinjaman Anda menunggu untuk dicairkan.",
                    "Mohon tunggu hingga dana selesai dicairkan. Anda dapat mengajukan penutupan akun kembali setelah dana diterima."
            ),
            7, new BlockCopy(
                    "Akun Anda belum dapat ditutup karena dana pinjaman Anda sedang dalam proses pencairan.",
                    "Mohon tunggu hingga dana selesai dicairkan. Anda dapat mengajukan penutupan akun kembali setelah dana diterima."
            )
    );

    private static final BlockCopy ON_LOAN_COPY = new BlockCopy(
            "Akun Anda belum dapat ditutup karena masih terdapat tagihan yang belum lunas.",
            "Silakan lunasi seluruh tagihan Anda terlebih dahulu. Anda dapat mengajukan penutupan akun kembali setelah seluruh tagihan lunas."
    );

    private final LenderUserStatusPort lenderUserStatusPort;

    public AccountCloseAccessFacade(LenderUserStatusPort lenderUserStatusPort) {
        this.lenderUserStatusPort = lenderUserStatusPort;
    }

    public AccountCloseAccessResult checkAccess(
            AuthenticatedPrincipal principal,
            LenderDeviceContext device
    ) {
        if (principal == null || principal.partnerUserId() == null || principal.partnerUserId().isBlank() || device == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        LenderUserStatusPort.LenderUserStatusResult status = lenderUserStatusPort.queryStatus(
                new LenderUserStatusPort.LenderUserStatusCommand(principal.partnerUserId(), device)
        );
        Integer lifeTimeStatus = status.userLoanLifeTimeStatus();
        if (BLOCKED_LIFE_TIME_STATUSES.contains(lifeTimeStatus)) {
            BlockCopy copy = LIFECYCLE_COPY.get(lifeTimeStatus);
            return AccountCloseAccessResult.blocked(copy.prompt(), copy.reason());
        }
        if (status.onLoanCount() != null && status.onLoanCount() != 0) {
            return AccountCloseAccessResult.blocked(ON_LOAN_COPY.prompt(), ON_LOAN_COPY.reason());
        }
        return AccountCloseAccessResult.allowed();
    }

    public record AccountCloseAccessResult(boolean canClose, String prompt, String reason) {
        static AccountCloseAccessResult allowed() {
            return new AccountCloseAccessResult(true, null, null);
        }

        static AccountCloseAccessResult blocked(String prompt, String reason) {
            return new AccountCloseAccessResult(false, prompt, reason);
        }
    }

    private record BlockCopy(String prompt, String reason) {
    }
}
