package com.pk.app.agreement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.infra.agreement.AgreementFieldConfigLoader;
import com.pk.infra.profile.ProfileQueryFacade;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgreementFieldApplicationServiceTest {
    private static final AuthenticatedPrincipal PRINCIPAL =
            new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L);

    private ProfileQueryFacade profileQueryFacade;
    private LoanQuoteRepository loanQuoteRepository;
    private AgreementFieldConfigLoader configLoader;
    private AgreementFieldApplicationService service;

    @BeforeEach
    void setUp() {
        profileQueryFacade = mock(ProfileQueryFacade.class);
        loanQuoteRepository = mock(LoanQuoteRepository.class);
        configLoader = mock(AgreementFieldConfigLoader.class);
        Clock clock = Clock.fixed(
                Instant.parse("2026-07-29T02:00:00Z"),
                ZoneId.of("Asia/Jakarta")
        );
        service = new AgreementFieldApplicationService(
                profileQueryFacade,
                loanQuoteRepository,
                configLoader,
                clock
        );
        when(configLoader.load()).thenReturn(config());
    }

    @Test
    void returnsProductSummaryFieldsFromConfiguration() {
        var response = service.productSummary(PRINCIPAL);

        assertThat(response.publisherName()).isEqualTo("PT Pendanaan Teknologi Nusa");
        assertThat(response.productType()).isEqualTo("Unsecured cash loan");
        assertThat(response.productDescription()).isEqualTo("Approved product description");
        assertThat(response.provisionFeeDisplay()).isEqualTo("Rp 0");
    }

    @Test
    void returnsStatementFieldsFromLenderProfile() throws Exception {
        when(profileQueryFacade.query(PRINCIPAL.partnerUserId(), java.util.List.of("identity")))
                .thenReturn(new ObjectMapper().readTree("""
                        {
                          "identity": {
                            "name": "OPEN USER",
                            "idNo": "3201010101010001"
                          }
                        }
                        """));

        var response = service.statement(PRINCIPAL);

        assertThat(response.borrowerName()).isEqualTo("OPEN USER");
        assertThat(response.borrowerNik()).isEqualTo("3201010101010001");
        assertThat(response.effectiveDateDisplay()).isEqualTo("29/07/2026");
    }

    @Test
    void returnsLoanPreviewFieldsFromProfileQuoteAndConfiguration() throws Exception {
        when(profileQueryFacade.query(
                PRINCIPAL.partnerUserId(),
                java.util.List.of("mobileNo", "identity", "bankCard")
        )).thenReturn(new ObjectMapper().readTree("""
                {
                  "mobileNo": {"mobileNo": "081234567890"},
                  "identity": {
                    "name": "OPEN USER",
                    "idNo": "3201010101010001",
                    "ocrResult": {
                      "address": "Central Jakarta",
                      "birthday": "1990-01-01"
                    }
                  },
                  "bankCardList": [
                    {"cardNumber": "111", "isDefault": false},
                    {"cardNumber": "222", "isDefault": true}
                  ]
                }
                """));
        LoanTrialQuoteDetail quote = mock(LoanTrialQuoteDetail.class);
        when(quote.fee2Name()).thenReturn("Electronic Signature Fee");
        when(quote.fee2()).thenReturn(new BigDecimal("12500"));
        when(quote.lendingDate()).thenReturn(1785290400000L);
        when(quote.lastRepayDate()).thenReturn(1798243200000L);
        when(loanQuoteRepository.findByQuoteNo("QUOTE-1")).thenReturn(Optional.of(
                new LoanQuoteRepository.LoanQuoteRecord(
                        1L,
                        "QUOTE-1",
                        PRINCIPAL.userId(),
                        2L,
                        null,
                        null,
                        quote,
                        Instant.parse("2026-07-29T02:00:00Z")
                )
        ));

        var response = service.loanPreview(PRINCIPAL, "QUOTE-1");

        assertThat(response.agreementNo()).isNull();
        assertThat(response.lenderName()).isEqualTo("Configured Lender");
        assertThat(response.borrowerName()).isEqualTo("OPEN USER");
        assertThat(response.borrowerAddress()).isEqualTo("Central Jakarta");
        assertThat(response.borrowerBirthDate()).isEqualTo("01/01/1990");
        assertThat(response.borrowerAge()).isEqualTo(36);
        assertThat(response.borrowerEmail()).isNull();
        assertThat(response.borrowerPhone()).isEqualTo("081234567890");
        assertThat(response.borrowerBankAccount()).isEqualTo("222");
        assertThat(response.borrowerAccountHolder()).isEqualTo("OPEN USER");
        assertThat(response.fundingPurpose()).isEqualTo("konsumtif multiguna");
        assertThat(response.eSignFeeDisplay()).isEqualTo("Rp 12.500");
        assertThat(response.effectiveDateDisplay()).isEmpty();
        assertThat(response.maturityDateDisplay()).isEmpty();
    }

    @Test
    void derivesAgreementDatesWhenQuoteDatesAreMissing() throws Exception {
        when(profileQueryFacade.query(
                PRINCIPAL.partnerUserId(),
                java.util.List.of("mobileNo", "identity", "bankCard")
        )).thenReturn(new ObjectMapper().createObjectNode());
        LoanTrialQuoteDetail quote = mock(LoanTrialQuoteDetail.class);
        when(quote.totalDays()).thenReturn(110L);
        when(loanQuoteRepository.findByQuoteNo("QUOTE-2")).thenReturn(Optional.of(
                new LoanQuoteRepository.LoanQuoteRecord(
                        2L,
                        "QUOTE-2",
                        PRINCIPAL.userId(),
                        2L,
                        null,
                        null,
                        quote,
                        Instant.parse("2026-07-29T02:00:00Z")
                )
        ));

        var response = service.loanPreview(PRINCIPAL, "QUOTE-2");

        assertThat(response.effectiveDateDisplay()).isEmpty();
        assertThat(response.maturityDateDisplay()).isEmpty();
    }

    private static AgreementFieldConfigLoader.AgreementFieldConfig config() {
        return new AgreementFieldConfigLoader.AgreementFieldConfig(
                "Configured Lender",
                "Laws of Indonesia",
                "Configured Address",
                "Authorized Representative",
                "Director",
                "PT Pendanaan Teknologi Nusa",
                "Unsecured cash loan",
                "Approved product description",
                "konsumtif multiguna",
                "Rp 0"
        );
    }
}
