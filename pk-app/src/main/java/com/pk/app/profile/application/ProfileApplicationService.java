package com.pk.app.profile.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.profile.dto.request.ProfileAppsFlyerInstallSaveRequest;
import com.pk.app.profile.dto.request.ProfileBankCardListAccessRequest;
import com.pk.app.profile.dto.request.ProfileBankCardDeleteRequest;
import com.pk.app.profile.dto.request.ProfileBankCardSaveRequest;
import com.pk.app.profile.dto.request.ProfileContactsSaveRequest;
import com.pk.app.profile.dto.request.ProfileLoginLogSaveRequest;
import com.pk.app.profile.dto.request.ProfileMobileChangeRequest;
import com.pk.app.profile.dto.request.ProfilePersonalSaveRequest;
import com.pk.app.profile.dto.request.ProfileTongdunDeviceSaveRequest;
import com.pk.app.profile.dto.response.ProfileAppsFlyerInstallSaveResponse;
import com.pk.app.profile.dto.response.ProfileBankCardListAccessResponse;
import com.pk.app.profile.dto.response.ProfileBankCardDeleteResponse;
import com.pk.app.profile.dto.response.ProfileBankCardSaveResponse;
import com.pk.app.profile.dto.response.ProfileContactsSaveResponse;
import com.pk.app.profile.dto.response.ProfileLoginLogSaveResponse;
import com.pk.app.profile.dto.response.ProfileMobileChangeResponse;
import com.pk.app.profile.dto.response.ProfilePersonalSaveResponse;
import com.pk.app.profile.dto.response.ProfileTongdunDeviceSaveResponse;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.BankCardListAccessFacade;
import com.pk.infra.profile.MobileChangeFacade;
import com.pk.infra.profile.ProfileServiceFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ProfileApplicationService {
    private final ProfileServiceFacade profileServiceFacade;
    private final BankCardListAccessFacade bankCardListAccessFacade;
    private final MobileChangeFacade mobileChangeFacade;
    private final PendanaanProperties pendanaanProperties;
    private final ObjectMapper objectMapper;

    public ProfileApplicationService(
            ProfileServiceFacade profileServiceFacade,
            BankCardListAccessFacade bankCardListAccessFacade,
            MobileChangeFacade mobileChangeFacade,
            PendanaanProperties pendanaanProperties,
            ObjectMapper objectMapper
    ) {
        this.profileServiceFacade = profileServiceFacade;
        this.bankCardListAccessFacade = bankCardListAccessFacade;
        this.mobileChangeFacade = mobileChangeFacade;
        this.pendanaanProperties = pendanaanProperties;
        this.objectMapper = objectMapper;
    }

    public ProfileMobileChangeResponse changeMobile(
            AuthenticatedPrincipal principal,
            ProfileMobileChangeRequest request
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        MobileChangeFacade.MobileChangeResult result =
                mobileChangeFacade.changeMobile(principal.userId(), request.newMobileNo());
        return new ProfileMobileChangeResponse(result.changed(), result.mobileNo());
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
                principal.userId(),
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
                principal.userId(),
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
                principal.userId(),
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

    public ProfileBankCardDeleteResponse deleteBankCard(
            AuthenticatedPrincipal principal,
            ProfileBankCardDeleteRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ProfileServiceFacade.BankCardDeleteResult result = profileServiceFacade.deleteBankCard(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new ProfileServiceFacade.BankCardDeleteCommand(
                        request.requestId(),
                        request.cardNumber(),
                        resolveDevice(request.device(), httpRequest)
                )
        );
        return new ProfileBankCardDeleteResponse(result.requestId(), result.deleted());
    }

    public ProfileBankCardListAccessResponse checkBankCardListAccess(
            AuthenticatedPrincipal principal,
            ProfileBankCardListAccessRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        BankCardListAccessFacade.BankCardListAccessResult result = bankCardListAccessFacade.checkAccess(
                principal.partnerUserId(),
                resolveDevice(request.device(), httpRequest)
        );
        return new ProfileBankCardListAccessResponse(result.canShowList());
    }

    public ProfileLoginLogSaveResponse saveLoginLog(
            AuthenticatedPrincipal principal,
            ProfileLoginLogSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ProfileServiceFacade.LoginLogSaveResult result = profileServiceFacade.saveLoginLog(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new ProfileServiceFacade.LoginLogSaveCommand(
                        request.requestId(),
                        request.loginType(),
                        request.loginIp(),
                        request.loginLat(),
                        request.loginLng(),
                        resolveDevice(request.device(), httpRequest)
                )
        );
        return new ProfileLoginLogSaveResponse(
                result.requestId(),
                result.moduleStatus(),
                parseLenderResponse(result.lenderResponseJson())
        );
    }

    public ProfileAppsFlyerInstallSaveResponse saveAppsFlyerInstall(
            AuthenticatedPrincipal principal,
            ProfileAppsFlyerInstallSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = principal == null ? null : principal.userId();
        String partnerUserId = principal == null ? null : principal.partnerUserId();
        String mobileNo = principal == null ? null : principal.mobileNo();
        ProfileServiceFacade.AppsFlyerSaveResult result = profileServiceFacade.saveAppsFlyerInstall(
                userId,
                partnerUserId,
                mobileNo,
                new ProfileServiceFacade.AppsFlyerSaveCommand(
                        request.requestId(),
                        request.appsflyerId(),
                        request.advertisingId(),
                        request.androidId(),
                        request.attributedTouchTime(),
                        request.gpClickTime(),
                        request.installTime(),
                        request.mediaSource(),
                        request.afPrt(),
                        request.afAdsetId(),
                        request.afAdset(),
                        request.afSiteid(),
                        request.afCId(),
                        request.campaign(),
                        request.appVersion(),
                        request.appId(),
                        request.deviceType(),
                        request.osVersion(),
                        request.countryCode(),
                        request.city(),
                        request.postalCode(),
                        request.ip(),
                        request.operator(),
                        request.deviceCategory(),
                        request.platform(),
                        request.deviceModel(),
                        request.idfv(),
                        request.idfa(),
                        request.afAd(),
                        request.afChannel(),
                        request.attributedTouchType(),
                        request.afAdId(),
                        request.afAdType(),
                        request.contributor1TouchType(),
                        request.contributor1TouchTime(),
                        request.contributor1AfPrt(),
                        request.contributor1MatchType(),
                        request.contributor1EngagementType(),
                        request.bundleId(),
                        request.matchType(),
                        request.gpInstallBegin(),
                        resolveDevice(request.device(), httpRequest)
                )
        );
        return new ProfileAppsFlyerInstallSaveResponse(
                result.requestId(),
                result.moduleStatus(),
                parseLenderResponse(result.lenderResponseJson())
        );
    }

    public ProfileTongdunDeviceSaveResponse saveTongdunDevice(
            AuthenticatedPrincipal principal,
            ProfileTongdunDeviceSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ProfileServiceFacade.TongdunSaveResult result = profileServiceFacade.saveTongdunDevice(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new ProfileServiceFacade.TongdunSaveCommand(
                        request.requestId(),
                        request.sceneType(),
                        request.tongdunKey(),
                        resolveDevice(request.device(), httpRequest)
                )
        );
        return new ProfileTongdunDeviceSaveResponse(
                result.requestId(),
                result.moduleStatus(),
                parseLenderResponse(result.lenderResponseJson())
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
