package com.pk.app.debug.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.debug.mapper.DebugUserProgressReadMapper;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DebugUserProgressApplicationServiceTest {
    @Test
    void returnsProgressAndRecentInteractionsByMobileNo() {
        UserAuthRepository userAuthRepository = mock(UserAuthRepository.class);
        OnboardingProgressFacade onboardingProgressFacade = mock(OnboardingProgressFacade.class);
        DebugUserProgressReadMapper readMapper = mock(DebugUserProgressReadMapper.class);
        DebugUserProgressApplicationService service = new DebugUserProgressApplicationService(
                userAuthRepository,
                onboardingProgressFacade,
                readMapper
        );
        when(userAuthRepository.findByMobileNo("801234567"))
                .thenReturn(Optional.of(new UserProfileSummary(10L, "U10001", "801234567", false)));
        when(onboardingProgressFacade.getProgress(10L, "U10001"))
                .thenReturn(new OnboardingProgressFacade.OnboardingProgressResult(
                        "U10001",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("personal", "identity"),
                        List.of("bankCard", "device", "contact")
                ));
        when(readMapper.findCreditApplyIds(10L)).thenReturn(List.of("AP-1"));
        when(readMapper.findLoanApplyIds(10L)).thenReturn(List.of("LN-1"));
        when(readMapper.findRecentInteractions(List.of("U10001", "801234567", "AP-1", "LN-1"), "U10001", "801234567", 80))
                .thenReturn(List.of(new DebugUserProgressReadMapper.InteractionRecord(
                        1L,
                        "pendanaan",
                        "NO-1",
                        "USER_STATUS",
                        "U10001",
                        "POST",
                        "/user/status",
                        "000000",
                        "success",
                        true,
                        120,
                        "{\"partnerUserId\":\"U10001\"}",
                        "{\"code\":\"000000\"}",
                        Instant.parse("2026-07-06T06:00:00Z")
                )));

        var response = service.query("801234567");

        assertThat(response.found()).isTrue();
        assertThat(response.user().profileId()).isEqualTo(10L);
        assertThat(response.progress().completedModules()).containsExactly("personal", "identity");
        assertThat(response.progress().missingModules()).containsExactly("bankCard", "device", "contact");
        assertThat(response.interactions()).hasSize(1);
        assertThat(response.interactions().getFirst().businessType()).isEqualTo("USER_STATUS");
    }

}
