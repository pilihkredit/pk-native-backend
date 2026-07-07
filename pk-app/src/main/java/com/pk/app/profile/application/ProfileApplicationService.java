package com.pk.app.profile.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.profile.dto.request.ProfileBankCardSaveRequest;
import com.pk.app.profile.dto.request.ProfileContactsSaveRequest;
import com.pk.app.profile.dto.request.ProfilePersonalSaveRequest;
import com.pk.app.profile.dto.response.ProfileBankCardSaveResponse;
import com.pk.app.profile.dto.response.ProfileContactsSaveResponse;
import com.pk.app.profile.dto.response.ProfilePersonalSaveResponse;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.ProfileServiceFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ProfileApplicationService {
    private final ProfileServiceFacade profileServiceFacade;
    private final PendanaanProperties pendanaanProperties;
    private final ObjectMapper objectMapper;

    public ProfileApplicationService(
            ProfileServiceFacade profileServiceFacade,
            PendanaanProperties pendanaanProperties,
            ObjectMapper objectMapper
    ) {
        this.profileServiceFacade = profileServiceFacade;
        this.pendanaanProperties = pendanaanProperties;
        this.objectMapper = objectMapper;
    }

    public ProfilePersonalSaveResponse savePersonal(
            AuthenticatedPrincipal principal,
            ProfilePersonalSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ProfileServiceFacade.PersonalSaveResult result = profileServiceFacade.savePersonal(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new ProfileServiceFacade.PersonalSaveCommand(
                        request.requestId(),
                        request.profile().educationDegree(),
                        request.profile().industry(),
                        request.profile().income(),
                        request.profile().motherSurname(),
                        request.profile().userEmail(),
                        resolveDevice(request.device(), httpRequest)
                )
        );
        return new ProfilePersonalSaveResponse(
                result.requestId(),
                result.moduleStatus(),
                parseLenderResponse(result.lenderResponseJson())
        );
    }

    public ProfileContactsSaveResponse saveContacts(
            AuthenticatedPrincipal principal,
            ProfileContactsSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ProfileServiceFacade.ContactsSaveResult result = profileServiceFacade.saveContacts(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new ProfileServiceFacade.ContactsSaveCommand(
                        request.requestId(),
                        request.contacts().stream()
                                .map(contact -> new ProfileServiceFacade.ContactItemCommand(
                                        contact.relationship(),
                                        contact.contactName(),
                                        contact.contactMobile()
                                ))
                                .toList(),
                        resolveDevice(request.device(), httpRequest)
                )
        );
        return new ProfileContactsSaveResponse(result.requestId(), result.moduleStatus());
    }

    public ProfileBankCardSaveResponse saveBankCard(
            AuthenticatedPrincipal principal,
            ProfileBankCardSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ProfileServiceFacade.BankCardSaveResult result = profileServiceFacade.saveBankCard(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new ProfileServiceFacade.BankCardSaveCommand(
                        request.requestId(),
                        request.bankCode(),
                        request.cardNumber(),
                        resolveDevice(request.device(), httpRequest)
                )
        );
        return new ProfileBankCardSaveResponse(
                result.requestId(),
                result.verifyStatus(),
                result.cardNoMasked()
        );
    }

    private LenderDeviceContext resolveDevice(
            com.pk.app.profile.dto.request.ProfileDeviceRequest deviceRequest,
            HttpServletRequest httpRequest
    ) {
        ClientRequestHeaders.ResolvedClientHeaders headers = ClientRequestHeaders.require(httpRequest);
        return ProfileDeviceSupport.resolveLenderDevice(deviceRequest, headers, pendanaanProperties);
    }

    private JsonNode parseLenderResponse(String lenderResponseJson) {
        if (lenderResponseJson == null || lenderResponseJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(lenderResponseJson);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
