package com.pk.app.area.application;

import com.pk.app.area.dto.response.AreaListItemResponse;
import com.pk.core.reference.AreaReference;
import com.pk.infra.reference.AreaReferenceFacade;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AreaApplicationService {
    private final AreaReferenceFacade areaReferenceFacade;

    public AreaApplicationService(AreaReferenceFacade areaReferenceFacade) {
        this.areaReferenceFacade = areaReferenceFacade;
    }

    public List<AreaListItemResponse> listAreas(String parentCode) {
        return areaReferenceFacade.listAreas(parentCode).stream()
                .map(AreaApplicationService::toResponse)
                .toList();
    }

    private static AreaListItemResponse toResponse(AreaReference area) {
        return new AreaListItemResponse(
                area.areaCode(),
                area.areaName(),
                area.parentCode(),
                area.level()
        );
    }
}
