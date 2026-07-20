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
        String partnerUserId = request.partnerUserId();
        Long profileId = null;
        if (principal != null) {
            profileId = principal.profileId();
            if (partnerUserId != null && !partnerUserId.isBlank()
                    && !partnerUserId.trim().equals(principal.partnerUserId())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            if (partnerUserId == null || partnerUserId.isBlank()) {
                partnerUserId = principal.partnerUserId();
            }
            if (request.mobileNo() != null
                    && !request.mobileNo().trim().equals(principal.mobileNo())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
        }
        var records = agreementFacade.createRecords(new AgreementFacade.CreateCommand(
                request.mobileNo(),
                partnerUserId,
                request.deviceNo(),
                profileId,
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
}
