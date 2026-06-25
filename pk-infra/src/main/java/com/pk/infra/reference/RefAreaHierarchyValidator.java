package com.pk.infra.reference;

import com.pk.core.profile.port.AreaHierarchyValidator;

public class RefAreaHierarchyValidator implements AreaHierarchyValidator {
    private final AreaReferenceFacade areaReferenceFacade;

    public RefAreaHierarchyValidator(AreaReferenceFacade areaReferenceFacade) {
        this.areaReferenceFacade = areaReferenceFacade;
    }

    @Override
    public void validateResidentialHierarchy(String provinceCode, String cityCode, String districtCode) {
        areaReferenceFacade.validateResidentialHierarchy(provinceCode, cityCode, districtCode);
    }
}
