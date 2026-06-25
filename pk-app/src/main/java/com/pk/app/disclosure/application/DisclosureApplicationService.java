package com.pk.app.disclosure.application;

import com.pk.app.disclosure.dto.response.DisclosurePermissionResponse;
import com.pk.infra.disclosure.DisclosureFacade;
import org.springframework.stereotype.Service;

@Service
public class DisclosureApplicationService {
    private final DisclosureFacade disclosureFacade;

    public DisclosureApplicationService(DisclosureFacade disclosureFacade) {
        this.disclosureFacade = disclosureFacade;
    }

    public DisclosurePermissionResponse getPermission(String scene, String acceptLanguage) {
        return DisclosurePermissionResponse.from(disclosureFacade.getPermission(scene, acceptLanguage));
    }
}
