package com.pk.app.area.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pk.core.reference.AreaReference;
import com.pk.infra.reference.AreaReferenceFacade;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AreaApplicationServiceTest {
    @Mock
    private AreaReferenceFacade areaReferenceFacade;

    @InjectMocks
    private AreaApplicationService areaApplicationService;

    @Test
    void mapsAreaReferencesToApiResponses() {
        when(areaReferenceFacade.listAreas("110000")).thenReturn(List.of(
                new AreaReference("110100", "Jakarta Selatan", "110000", 2)
        ));

        var responses = areaApplicationService.listAreas("110000");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().areaCode()).isEqualTo("110100");
        assertThat(responses.getFirst().areaName()).isEqualTo("Jakarta Selatan");
        assertThat(responses.getFirst().parentCode()).isEqualTo("110000");
        assertThat(responses.getFirst().level()).isEqualTo(2);
    }
}
