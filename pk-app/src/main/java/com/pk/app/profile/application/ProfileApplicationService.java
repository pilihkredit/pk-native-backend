package com.pk.app.profile.application;

import com.pk.app.profile.dto.request.ProfilePersonalSaveRequest;
import com.pk.app.profile.dto.response.ProfilePersonalSaveResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.profile.ProfileServiceFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileApplicationService {
    private final ProfileServiceFacade profileServiceFacade;

    public ProfileApplicationService(ProfileServiceFacade profileServiceFacade) {
        this.profileServiceFacade = profileServiceFacade;
    }

    @Transactional
    public ProfilePersonalSaveResponse savePersonal(
            AuthenticatedPrincipal principal,
            ProfilePersonalSaveRequest request
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ProfileServiceFacade.PersonalSaveResult result = profileServiceFacade.savePersonal(
                principal.profileId(),
                new ProfileServiceFacade.PersonalSaveCommand(
                        request.requestId(),
                        request.provinceCode(),
                        request.cityCode(),
                        request.districtCode(),
                        request.address(),
                        request.educationDegree(),
                        request.motherSurname(),
                        request.userEmail()
                )
        );
        return new ProfilePersonalSaveResponse(result.requestId(), result.moduleStatus());
    }
}
