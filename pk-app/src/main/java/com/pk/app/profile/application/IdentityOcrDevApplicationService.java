package com.pk.app.profile.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.dto.request.IdentityOcrDevLenderSyncRequest;
import com.pk.app.profile.dto.response.IdentityOcrDevLenderSyncResponse;
import com.pk.app.profile.support.DevIdentityOcrDefaults;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.profile.IdentityOcrFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "pk.ocr", name = "dev-lender-sync-enabled", havingValue = "true")
public class IdentityOcrDevApplicationService {
    private final IdentityOcrFacade identityOcrFacade;
    private final PendanaanProperties pendanaanProperties;

    public IdentityOcrDevApplicationService(
            IdentityOcrFacade identityOcrFacade,
            PendanaanProperties pendanaanProperties
    ) {
        this.identityOcrFacade = identityOcrFacade;
        this.pendanaanProperties = pendanaanProperties;
    }

    public IdentityOcrDevLenderSyncResponse syncToLender(
            AuthenticatedPrincipal principal,
            IdentityOcrDevLenderSyncRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ClientRequestHeaders.ResolvedClientHeaders headers = ClientRequestHeaders.require(httpRequest);
        RequestTrace.resolveClientRequestId(httpRequest, request.requestId());
        boolean captureOcrFromIdCard = hasText(request.idCardBase64()) && !hasText(request.rawOcrDetail());
        IdentityOcrFacade.DevLenderSyncResult result = identityOcrFacade.devSyncIdentityToLender(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new IdentityOcrFacade.DevLenderSyncCommand(
                        request.requestId(),
                        request.faceBase64(),
                        request.idCardBase64(),
                        request.rawOcrDetail(),
                        captureOcrFromIdCard
                                ? request.ocrName()
                                : DevIdentityOcrDefaults.orDefault(request.ocrName(), DevIdentityOcrDefaults.OCR_NAME),
                        captureOcrFromIdCard
                                ? request.ocrIdNo()
                                : DevIdentityOcrDefaults.orDefault(request.ocrIdNo(), DevIdentityOcrDefaults.OCR_ID_NO),
                        captureOcrFromIdCard
                                ? request.gender()
                                : DevIdentityOcrDefaults.orDefault(request.gender(), DevIdentityOcrDefaults.GENDER),
                        captureOcrFromIdCard
                                ? request.religion()
                                : DevIdentityOcrDefaults.orDefault(request.religion(), DevIdentityOcrDefaults.RELIGION),
                        captureOcrFromIdCard
                                ? request.maritalStatus()
                                : DevIdentityOcrDefaults.orDefault(
                                        request.maritalStatus(),
                                        DevIdentityOcrDefaults.MARITAL_STATUS
                                ),
                        captureOcrFromIdCard
                                ? request.birthday()
                                : DevIdentityOcrDefaults.orDefault(request.birthday(), DevIdentityOcrDefaults.BIRTHDAY),
                        captureOcrFromIdCard
                                ? request.birthPlace()
                                : DevIdentityOcrDefaults.orDefault(request.birthPlace(), DevIdentityOcrDefaults.BIRTH_PLACE),
                        captureOcrFromIdCard
                                ? request.address()
                                : DevIdentityOcrDefaults.orDefault(request.address(), DevIdentityOcrDefaults.ADDRESS),
                        captureOcrFromIdCard
                                ? request.occupation()
                                : DevIdentityOcrDefaults.orDefault(request.occupation(), DevIdentityOcrDefaults.OCCUPATION),
                        captureOcrFromIdCard
                                ? request.nationality()
                                : DevIdentityOcrDefaults.orDefault(request.nationality(), DevIdentityOcrDefaults.NATIONALITY),
                        captureOcrFromIdCard
                                ? request.bloodType()
                                : DevIdentityOcrDefaults.orDefault(request.bloodType(), DevIdentityOcrDefaults.BLOOD_TYPE),
                        captureOcrFromIdCard
                                ? request.expiryDate()
                                : DevIdentityOcrDefaults.orDefault(request.expiryDate(), DevIdentityOcrDefaults.EXPIRY_DATE),
                        captureOcrFromIdCard
                                ? request.province()
                                : DevIdentityOcrDefaults.orDefault(request.province(), DevIdentityOcrDefaults.PROVINCE),
                        captureOcrFromIdCard
                                ? request.city()
                                : DevIdentityOcrDefaults.orDefault(request.city(), DevIdentityOcrDefaults.CITY),
                        captureOcrFromIdCard
                                ? request.district()
                                : DevIdentityOcrDefaults.orDefault(request.district(), DevIdentityOcrDefaults.DISTRICT),
                        ProfileDeviceSupport.resolveLenderDevice(request.device(), headers, pendanaanProperties)
                )
        );
        return new IdentityOcrDevLenderSyncResponse(result.requestId(), result.lenderResponse());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
