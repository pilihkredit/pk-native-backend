package com.pk.app.agreement.application;

import com.pk.app.agreement.dto.request.AgreementCreateRequest;
import com.pk.app.agreement.dto.request.AgreementLatestRequest;
import com.pk.app.agreement.dto.response.AgreementCreateResponse;
import com.pk.app.agreement.dto.response.AgreementLatestResponse;
import com.pk.app.agreement.dto.response.AgreementRecordResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.agreement.AgreementFacade;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AgreementApplicationService {
    private final AgreementFacade agreementFacade;
    private final UserAuthRepository userAuthRepository;

    public AgreementApplicationService(
            AgreementFacade agreementFacade,
            UserAuthRepository userAuthRepository
    ) {
        this.agreementFacade = agreementFacade;
        this.userAuthRepository = userAuthRepository;
    }

    public AgreementCreateResponse create(AuthenticatedPrincipal principal, AgreementCreateRequest request) {
        String partnerUserId = request.partnerUserId();
        Long profileId = null;
        String mobileNo = null;

        if (principal != null) {
            profileId = principal.profileId();
            mobileNo = principal.mobileNo();
            if (partnerUserId != null && !partnerUserId.isBlank()
                    && !partnerUserId.trim().equals(principal.partnerUserId())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            if (partnerUserId == null || partnerUserId.isBlank()) {
                partnerUserId = principal.partnerUserId();
            }
        } else if (partnerUserId != null && !partnerUserId.isBlank()) {
            UserProfileSummary profile = userAuthRepository.findByPartnerUserId(partnerUserId.trim())
                    .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
            profileId = profile.profileId();
            mobileNo = profile.mobileNo();
            partnerUserId = profile.partnerUserId();
        }

        if (mobileNo == null || mobileNo.isBlank()) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }

        var records = agreementFacade.createRecords(new AgreementFacade.CreateCommand(
                mobileNo,
                partnerUserId,
                request.deviceNo(),
                profileId,
                Instant.ofEpochMilli(request.clickedAt()),
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
