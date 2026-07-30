package com.pk.app.loan.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.infra.loan.LoanProductFacade;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanProductApplicationServiceTest {
    @Mock
    private LoanProductFacade loanProductFacade;

    @Mock
    private ReviewSandboxConfigPort reviewSandboxConfigPort;

    @Test
    void forcesTemplateRefreshForReviewUser() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(1L, "USER-1", "81200000000", 1L);
        when(reviewSandboxConfigPort.findEnabledScenario(principal.mobileNo()))
                .thenReturn(Optional.of(scenario()));
        when(loanProductFacade.listProductsForceRefresh(1L, "APPLY-1"))
                .thenReturn(new LoanProductFacade.ProductsResult("APPLY-1", "APPROVED", "READY", List.of()));
        var service = new LoanProductApplicationService(loanProductFacade, reviewSandboxConfigPort);

        service.listProducts(principal, "APPLY-1");

        verify(loanProductFacade).listProductsForceRefresh(1L, "APPLY-1");
    }

    @Test
    void keepsCachedProductPathForNormalUser() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(2L, "USER-2", "81300000000", 1L);
        when(reviewSandboxConfigPort.findEnabledScenario(principal.mobileNo())).thenReturn(Optional.empty());
        when(loanProductFacade.listProducts(2L, "APPLY-2"))
                .thenReturn(new LoanProductFacade.ProductsResult("APPLY-2", "APPROVED", "READY", List.of()));
        var service = new LoanProductApplicationService(loanProductFacade, reviewSandboxConfigPort);

        service.listProducts(principal, "APPLY-2");

        verify(loanProductFacade).listProducts(2L, "APPLY-2");
    }

    private static ReviewSandboxConfigPort.ReviewSandboxScenario scenario() {
        return new ReviewSandboxConfigPort.ReviewSandboxScenario(
                "APP_STORE", new BigDecimal("200000"), new BigDecimal("6000000"),
                new BigDecimal("200000"), new BigDecimal("0.003"), BigDecimal.ONE,
                3, 30, "BANK_A", "Bank A", "1111111111111"
        );
    }
}
