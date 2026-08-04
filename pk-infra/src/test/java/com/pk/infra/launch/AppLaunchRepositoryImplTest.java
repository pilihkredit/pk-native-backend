package com.pk.infra.launch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.launch.AppLaunchRecord;
import com.pk.infra.launch.mapper.AppLaunchMapper;
import com.pk.infra.launch.repository.AppLaunchRepositoryImpl;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AppLaunchRepositoryImplTest {
    @Test
    void recordsNewEvent() {
        AppLaunchMapper mapper = mock(AppLaunchMapper.class);
        AppLaunchRecord record = sampleRecord();
        when(mapper.insertEvent(record)).thenReturn(1);

        boolean recorded = new AppLaunchRepositoryImpl(mapper).record(record);

        org.assertj.core.api.Assertions.assertThat(recorded).isTrue();
    }

    @Test
    void ignoresDuplicateLaunch() {
        AppLaunchMapper mapper = mock(AppLaunchMapper.class);
        AppLaunchRecord record = sampleRecord();
        when(mapper.insertEvent(record)).thenReturn(0);

        boolean recorded = new AppLaunchRepositoryImpl(mapper).record(record);

        org.assertj.core.api.Assertions.assertThat(recorded).isFalse();
    }

    private static AppLaunchRecord sampleRecord() {
        Instant occurredAt = Instant.parse("2026-08-05T00:00:00Z");
        return new AppLaunchRecord(
                "launch-1",
                "device-1",
                null,
                "1.0.0",
                "android",
                "com.example.app",
                occurredAt,
                occurredAt
        );
    }
}
