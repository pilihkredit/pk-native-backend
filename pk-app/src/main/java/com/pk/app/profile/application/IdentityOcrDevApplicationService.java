package com.pk.app.profile.application;

import com.pk.app.common.web.ClientRequestHeaders;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public IdentityOcrDevLenderSyncResponse syncToLender(
            AuthenticatedPrincipal principal,
            IdentityOcrDevLenderSyncRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        ClientRequestHeaders.ResolvedClientHeaders headers = ClientRequestHeaders.require(httpRequest);
        IdentityOcrFacade.DevLenderSyncResult result = identityOcrFacade.devSyncIdentityToLender(
                principal.profileId(),
                principal.partnerUserId(),
                new IdentityOcrFacade.DevLenderSyncCommand(
                        request.requestId(),
                        request.faceBase64(),
                        request.idCardBase64(),
                        DevIdentityOcrDefaults.orDefault(request.ocrName(), DevIdentityOcrDefaults.OCR_NAME),
                        DevIdentityOcrDefaults.orDefault(request.ocrIdNo(), DevIdentityOcrDefaults.OCR_ID_NO),
                        DevIdentityOcrDefaults.orDefault(request.gender(), DevIdentityOcrDefaults.GENDER),
                        DevIdentityOcrDefaults.orDefault(request.religion(), DevIdentityOcrDefaults.RELIGION),
                        DevIdentityOcrDefaults.orDefault(request.maritalStatus(), DevIdentityOcrDefaults.MARITAL_STATUS),
                        DevIdentityOcrDefaults.orDefault(request.birthday(), DevIdentityOcrDefaults.BIRTHDAY),
                        DevIdentityOcrDefaults.orDefault(request.birthPlace(), DevIdentityOcrDefaults.BIRTH_PLACE),
                        DevIdentityOcrDefaults.orDefault(request.address(), DevIdentityOcrDefaults.ADDRESS),
                        DevIdentityOcrDefaults.orDefault(request.occupation(), DevIdentityOcrDefaults.OCCUPATION),
                        DevIdentityOcrDefaults.orDefault(request.nationality(), DevIdentityOcrDefaults.NATIONALITY),
                        DevIdentityOcrDefaults.orDefault(request.bloodType(), DevIdentityOcrDefaults.BLOOD_TYPE),
                        DevIdentityOcrDefaults.orDefault(request.expiryDate(), DevIdentityOcrDefaults.EXPIRY_DATE),
                        DevIdentityOcrDefaults.orDefault(request.province(), DevIdentityOcrDefaults.PROVINCE),
                        DevIdentityOcrDefaults.orDefault(request.city(), DevIdentityOcrDefaults.CITY),
                        DevIdentityOcrDefaults.orDefault(request.district(), DevIdentityOcrDefaults.DISTRICT),
                        ProfileDeviceSupport.resolveLenderDevice(request.device(), headers, pendanaanProperties)
                )
        );
        return new IdentityOcrDevLenderSyncResponse(result.requestId(), result.lenderResponse());
    }
}
