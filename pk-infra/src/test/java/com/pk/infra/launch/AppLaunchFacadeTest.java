package com.pk.infra.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.launch.AppLaunchRecord;
import com.pk.core.launch.port.AppLaunchRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AppLaunchFacadeTest {
    @Test
    void recordsLaunchWithOptionalIdfv() {
        AppLaunchRepository repository = mock(AppLaunchRepository.class);
        AppLaunchRecord record = sampleRecord("idfv-1");
        when(repository.record(record)).thenReturn(true);

        boolean recorded = new AppLaunchFacade(repository).record(record);

        assertThat(recorded).isTrue();
        ArgumentCaptor<AppLaunchRecord> captor = ArgumentCaptor.forClass(AppLaunchRecord.class);
        verify(repository).record(captor.capture());
        assertThat(captor.getValue().idfv()).isEqualTo("idfv-1");
    }

    @Test
    void rejectsIdfvLongerThan128() {
        AppLaunchRepository repository = mock(AppLaunchRepository.class);
        AppLaunchRecord record = sampleRecord("x".repeat(129));

        assertThatThrownBy(() -> new AppLaunchFacade(repository).record(record))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    private static AppLaunchRecord sampleRecord(String idfv) {
        Instant occurredAt = Instant.parse("2026-08-05T00:00:00Z");
        return new AppLaunchRecord(
                "launch-1",
                "device-1",
                null,
                "1.0.0",
                "ios",
                "com.example.app",
                idfv,
                occurredAt,
                occurredAt
        );
    }
}
