package com.pk.infra.push;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.pk.core.push.PushDeviceRegistration;
import com.pk.core.push.PushPermissionStatus;
import com.pk.core.push.port.PushDeviceRepository;
import org.junit.jupiter.api.Test;

class PushDeviceFacadeTest {
    @Test
    void registersAnonymousDevice() {
        PushDeviceRepository repository = mock(PushDeviceRepository.class);
        PushDeviceRegistration registration = new PushDeviceRegistration(
                null,
                "device-1",
                "1.0.0",
                "android",
                "com.example.app",
                "fcm-token",
                PushPermissionStatus.GRANTED
        );

        new PushDeviceFacade(repository).register(registration);

        verify(repository).register(registration);
    }

    @Test
    void bindsAndUnbindsOnlyWhenDeviceNoIsPresent() {
        PushDeviceRepository repository = mock(PushDeviceRepository.class);
        PushDeviceFacade facade = new PushDeviceFacade(repository);

        facade.bindUser(10L, "device-1");
        facade.unbindUser(10L, "device-1");

        verify(repository).bindUser(10L, "device-1");
        verify(repository).unbindUser(10L, "device-1");
    }
}
