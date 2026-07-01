package com.pk.app.credit.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.credit.dto.request.CreditAppInfoRequest;
import com.pk.app.credit.dto.request.CreditApplyRequest;
import com.pk.app.credit.dto.response.CreditApplyResponse;
import com.pk.app.credit.dto.response.CreditLimitDisplayResponse;
import com.pk.app.credit.dto.response.CreditStatusResponse;
import com.pk.app.profile.application.ProfileDeviceSupport;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.credit.CreditApplyFacade;
import com.pk.infra.credit.CreditLimitDisplayFacade;
import com.pk.infra.profile.UserDeviceWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditApplicationService {
    private final CreditApplyFacade creditApplyFacade;
    private final CreditLimitDisplayFacade creditLimitDisplayFacade;
    private final PendanaanProperties pendanaanProperties;
    private final UserDeviceWriter userDeviceWriter;

    public CreditApplicationService(
            CreditApplyFacade creditApplyFacade,
            CreditLimitDisplayFacade creditLimitDisplayFacade,
            PendanaanProperties pendanaanProperties,
            UserDeviceWriter userDeviceWriter
    ) {
        this.creditApplyFacade = creditApplyFacade;
        this.creditLimitDisplayFacade = creditLimitDisplayFacade;
        this.pendanaanProperties = pendanaanProperties;
        this.userDeviceWriter = userDeviceWriter;
    }

    @Transactional
    public CreditApplyResponse apply(
            AuthenticatedPrincipal principal,
            CreditApplyRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        LenderDeviceContext device = ProfileDeviceSupport.resolveRiskLenderDevice(
                request.riskDataInfo().openUserDevice(),
                ClientRequestHeaders.require(httpRequest),
                pendanaanProperties
        );
        userDeviceWriter.upsertFromRequest(
                principal.profileId(),
                principal.partnerUserId(),
                request.requestId(),
                device
        );
        CreditApplyFacade.ApplyResult result = creditApplyFacade.apply(
                principal.profileId(),
                principal.partnerUserId(),
                new CreditApplyFacade.ApplyCommand(
                        request.requestId(),
                        request.lat(),
                        request.lng(),
                        request.ip(),
                        request.address(),
                        device,
                        toAppList(request.riskDataInfo().appList())
                )
        );
        return new CreditApplyResponse(result.applyId(), result.status(), result.creditApplyNo());
    }

    public CreditStatusResponse getStatus(AuthenticatedPrincipal principal, String applyId) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        CreditApplyFacade.StatusResult result = creditApplyFacade.getStatus(principal.profileId(), applyId);
        return new CreditStatusResponse(
                result.applyId(),
                result.status(),
                result.creditApplyNo(),
                result.creditContractExpireTime(),
                result.freezeEndTime(),
                result.riskMinLimit(),
                result.riskMaxLimit(),
                result.psychologicalCreditLimit(),
                result.fakeCreditLimit(),
                result.borrowAmtStepSize()
        );
    }

    public CreditLimitDisplayResponse getLimitDisplay(
            AuthenticatedPrincipal principal,
            String applyId,
            String repayMethod
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return CreditLimitDisplayResponse.from(
                creditLimitDisplayFacade.getLimitDisplay(principal.profileId(), applyId, repayMethod)
        );
    }

    private static List<CreditRiskAppInfo> toAppList(List<CreditAppInfoRequest> appList) {
        return appList.stream()
                .map(app -> new CreditRiskAppInfo(
                        app.appName(),
                        app.packageName(),
                        app.appFlags(),
                        app.appType(),
                        app.versionCode(),
                        app.versionName(),
                        app.inTime(),
                        app.upTime()
                ))
                .toList();
    }
}
