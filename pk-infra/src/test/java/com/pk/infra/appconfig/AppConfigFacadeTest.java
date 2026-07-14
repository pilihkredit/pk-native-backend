package com.pk.infra.appconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppConfigFacadeTest {
    @Mock
    private AppConfigRepository appConfigRepository;

    private AppConfigFacade facade;

    @BeforeEach
    void setUp() {
        facade = new AppConfigFacade(appConfigRepository, new ObjectMapper());
    }

    @Test
    void getValueByKeyReturnsParsedJson() {
        when(appConfigRepository.findByKey("home.banner")).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(1L, "home.banner", "{\"title\":\"Hi\"}")
        ));

        assertThat(facade.getValueByKey("home.banner").get("title").asText()).isEqualTo("Hi");
    }

    @Test
    void getValueByKeyRejectsBlankKey() {
        assertThatThrownBy(() -> facade.getValueByKey(" "))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void getValueByKeyRejectsUnknownKey() {
        when(appConfigRepository.findByKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getValueByKey("missing"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }
}
