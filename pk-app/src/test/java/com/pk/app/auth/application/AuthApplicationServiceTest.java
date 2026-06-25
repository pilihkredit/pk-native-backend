package com.pk.app.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.app.auth.dto.request.MobileCheckRequest;
import com.pk.app.home.application.HomeApplicationService;
import com.pk.infra.auth.AuthServiceFacade;
import org.junit.jupiter.api.Test;

class AuthApplicationServiceTest {
    @Test
    void mapsMobileCheckResultToResponse() {
        AuthServiceFacade facade = mock(AuthServiceFacade.class);
        HomeApplicationService homeApplicationService = mock(HomeApplicationService.class);
        when(facade.checkMobileRegistration("8123456789", "device-1"))
                .thenReturn(new AuthServiceFacade.MobileCheckResult(true, "EXISTING", true));

        MobileCheckRequest request = new MobileCheckRequest("8123456789", "device-1");
        var response = new AuthApplicationService(facade, homeApplicationService).checkMobile(request, "device-1");

        assertThat(response.registered()).isTrue();
        assertThat(response.accountStatus()).isEqualTo("EXISTING");
        assertThat(response.passwordSet()).isTrue();
        verify(facade).checkMobileRegistration("8123456789", "device-1");
    }
}
