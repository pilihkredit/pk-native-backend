package com.pk.infra.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.reference.AreaReference;
import com.pk.core.reference.port.LenderAreaPort;
import com.pk.core.reference.port.RefAreaRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AreaReferenceFacadeTest {
    private RefAreaRepository refAreaRepository;
    private LenderAreaPort lenderAreaPort;
    private AreaReferenceFacade facade;

    @BeforeEach
    void setUp() {
        refAreaRepository = org.mockito.Mockito.mock(RefAreaRepository.class);
        lenderAreaPort = org.mockito.Mockito.mock(LenderAreaPort.class);
        ReferenceProperties properties = new ReferenceProperties();
        properties.setAreaCacheTtl(Duration.ofHours(24));
        facade = new AreaReferenceFacade(refAreaRepository, lenderAreaPort, properties);
    }

    @Test
    void returnsCachedProvincesWithoutCallingLenderWhenFresh() {
        List<AreaReference> cached = sampleProvinces();
        when(refAreaRepository.findActiveByParentCode("")).thenReturn(cached);
        when(refAreaRepository.findLatestSyncedAtByParent("")).thenReturn(Optional.of(Instant.now()));

        List<AreaReference> result = facade.listAreas(null);

        assertThat(result).isEqualTo(cached);
        verify(lenderAreaPort, never()).listAreas(any());
    }

    @Test
    void syncsFromLenderWhenCacheEmpty() {
        when(refAreaRepository.findActiveByParentCode("")).thenReturn(List.of(), sampleProvinces());
        when(refAreaRepository.findLatestSyncedAtByParent("")).thenReturn(Optional.empty());
        when(lenderAreaPort.listAreas(null)).thenReturn(sampleProvinces());

        List<AreaReference> result = facade.listAreas("");

        assertThat(result).hasSize(2);
        verify(refAreaRepository).replaceChildrenByParent(eq(""), eq(sampleProvinces()), any());
    }

    @Test
    void assignsChildLevelFromParentWhenSyncingCities() {
        when(refAreaRepository.existsActiveAreaCode("110000")).thenReturn(true);
        when(refAreaRepository.findActiveByParentCode("110000")).thenReturn(List.of(), List.of(
                new AreaReference("110100", "Jakarta Selatan", "110000", 2)
        ));
        when(refAreaRepository.findLatestSyncedAtByParent("110000")).thenReturn(Optional.empty());
        when(refAreaRepository.findActiveByCode("110000"))
                .thenReturn(Optional.of(new AreaReference("110000", "Jakarta", "", 1)));
        when(lenderAreaPort.listAreas("110000")).thenReturn(List.of(
                new AreaReference("110100", "Jakarta Selatan", "110000", 0)
        ));

        List<AreaReference> result = facade.listAreas("110000");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().level()).isEqualTo(2);
    }

    @Test
    void rejectsUnknownParentCode() {
        when(refAreaRepository.existsActiveAreaCode("999999")).thenReturn(false);

        assertThatThrownBy(() -> facade.listAreas("999999"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void throwsServiceUnavailableWhenLenderReturnsEmpty() {
        when(refAreaRepository.existsActiveAreaCode("110000")).thenReturn(true);
        when(refAreaRepository.findActiveByParentCode("110000")).thenReturn(List.of());
        when(refAreaRepository.findLatestSyncedAtByParent("110000")).thenReturn(Optional.empty());
        when(lenderAreaPort.listAreas("110000")).thenReturn(List.of());

        assertThatThrownBy(() -> facade.listAreas("110000"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);
    }

    private static List<AreaReference> sampleProvinces() {
        return List.of(
                new AreaReference("110000", "Jakarta", "", 1),
                new AreaReference("320000", "Jawa Barat", "", 1)
        );
    }
}
