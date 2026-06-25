package com.pk.app.disclosure.dto.response;

import com.pk.infra.disclosure.DisclosureFacade;
import java.util.List;

public record DisclosurePermissionResponse(
        String disclosureId,
        String version,
        String locale,
        List<String> bodyParagraphs,
        String agreeButtonText,
        String disagreeButtonText,
        boolean mustAgree,
        String iconType,
        long updatedAt
) {
    public static DisclosurePermissionResponse from(DisclosureFacade.PermissionResult result) {
        return new DisclosurePermissionResponse(
                result.disclosureId(),
                result.version(),
                result.locale(),
                result.bodyParagraphs(),
                result.agreeButtonText(),
                result.disagreeButtonText(),
                result.mustAgree(),
                result.iconType(),
                result.updatedAt()
        );
    }
}
