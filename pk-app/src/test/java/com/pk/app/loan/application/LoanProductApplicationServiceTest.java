package com.pk.app.loan.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.loan.LoanProductFacade;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanProductApplicationServiceTest {
    @Mock
    private LoanProductFacade loanProductFacade;

    @Test
    void usesStandardProductResolutionForEveryUser() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(1L, "USER-1", "81200000000", 1L);
        when(loanProductFacade.listProducts(1L, "APPLY-1"))
                .thenReturn(new LoanProductFacade.ProductsResult("APPLY-1", "APPROVED", "READY", List.of()));
        var service = new LoanProductApplicationService(loanProductFacade);

        service.listProducts(principal, "APPLY-1");

        verify(loanProductFacade).listProducts(1L, "APPLY-1");
    }
}
