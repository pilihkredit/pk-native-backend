package com.pk.app.agreement.application;

import com.pk.app.agreement.dto.request.AgreementCreateRequest;
import com.pk.app.agreement.dto.request.AgreementLatestRequest;
import com.pk.app.agreement.dto.response.AgreementCreateResponse;
import com.pk.app.agreement.dto.response.AgreementLatestResponse;
import com.pk.app.agreement.dto.response.AgreementRecordResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.agreement.AgreementFacade;
import org.springframework.stereotype.Service;

@Service
public class AgreementApplicationService {
    private final AgreementFacade agreementFacade;

    public AgreementApplicationService(AgreementFacade agreementFacade) {
        this.agreementFacade = agreementFacade;
    }

    public AgreementCreateResponse create(AuthenticatedPrincipal principal, AgreementCreateRequest request) {
        String partnerUserId = blankToNull(request.partnerUserId());
        Long userId = null;
        String mobileNo = null;

        if (principal != null) {
            userId = principal.userId();
            mobileNo = principal.mobileNo();
            if (partnerUserId != null && !partnerUserId.equals(principal.partnerUserId())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            if (partnerUserId == null) {
                partnerUserId = principal.partnerUserId();
            }
        }
        // Not logged in: partnerUserId / mobileNo are both optional; store what was sent (if any).

        var records = agreementFacade.createRecords(new AgreementFacade.CreateCommand(
                mobileNo,
                partnerUserId,
                request.deviceNo(),
                userId,
                request.clickedAt(),
                request.items().stream()
                        .map(item -> new AgreementFacade.AgreementItemCommand(
                                item.agreementType(),
                                item.agreed()
                        ))
                        .toList()
        ));
        return new AgreementCreateResponse(AgreementRecordResponse.fromList(records));
    }

    public AgreementLatestResponse latest(AgreementLatestRequest request) {
        var records = agreementFacade.latestByMobileNo(request.mobileNo(), request.agreementTypes());
        return new AgreementLatestResponse(AgreementRecordResponse.fromList(records));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
