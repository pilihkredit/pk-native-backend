package com.pk.infra.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.home.HomeUserStage;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.home.port.UserLenderStatusQueryRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HomeSummaryFacadeTest {
    private OnboardingProgressFacade onboardingProgressFacade;
    private LenderUserStatusPort lenderUserStatusPort;
    private UserLenderStatusQueryRepository userLenderStatusQueryRepository;
    private HomeSummaryFacade facade;

    @BeforeEach
    void setUp() {
        onboardingProgressFacade = mock(OnboardingProgressFacade.class);
        lenderUserStatusPort = mock(LenderUserStatusPort.class);
        userLenderStatusQueryRepository = mock(UserLenderStatusQueryRepository.class);
        facade = new HomeSummaryFacade(
                onboardingProgressFacade,
                lenderUserStatusPort,
                userLenderStatusQueryRepository
        );
    }

    @Test
    void syncsLenderUserStatusAndPersistsSnapshot() {
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(
                new LenderUserStatusPort.LenderUserStatusResult(
                        "U10001",
                        "USR-1",
                        4,
                        22,
                        null,
                        0,
                        1780300800000L,
                        "{\"partnerUserId\":\"U10001\"}",
                        "{\"userId\":\"USR-1\"}"
                )
        );

        var result = facade.getSummary(1L, "U10001", "81234567890", sampleDevice());

        assertThat(result.partnerUserId()).isEqualTo("U10001");
        assertThat(result.userId()).isEqualTo("USR-1");
        assertThat(result.userLoanLifeTimeStatus()).isEqualTo(4);
        assertThat(result.userLoanLifeTimeLastAction()).isEqualTo(22);
        assertThat(result.onLoanCount()).isZero();
        assertThat(result.creditContractExpireTime()).isEqualTo(1780300800000L);
        verify(userLenderStatusQueryRepository).upsert(any());
    }

    @Test
    void resolveLocalUserStageReturnsOnboardingWhenIncomplete() {
        when(onboardingProgressFacade.getProgress(1L, "U10001")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "U10001",
                        OnboardingProgressFacade.KYC_INCOMPLETE,
                        List.of(),
                        List.of("personal")
                )
        );

        assertThat(facade.resolveLocalUserStage(1L, "U10001")).isEqualTo(HomeUserStage.ONBOARDING);
    }

    @Test
    void resolveLocalUserStageReturnsReadyWhenSynced() {
        when(onboardingProgressFacade.getProgress(1L, "U10001")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "U10001",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("personal"),
                        List.of()
                )
        );

        assertThat(facade.resolveLocalUserStage(1L, "U10001")).isEqualTo(HomeUserStage.READY);
    }

    private static LenderDeviceContext sampleDevice() {
        return new LenderDeviceContext(
                "LenderApp",
                "1.0.0",
                "com.example",
                "device-1",
                "android",
                null,
                null,
                "ClientApp"
        );
    }
}
