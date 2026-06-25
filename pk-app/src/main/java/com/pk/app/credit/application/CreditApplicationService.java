package com.pk.app.credit.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.credit.dto.request.CreditAppInfoRequest;
import com.pk.app.credit.dto.request.CreditApplyRequest;
import com.pk.app.credit.dto.response.CreditApplyResponse;
import com.pk.app.credit.dto.response.CreditStatusResponse;
import com.pk.app.profile.application.ProfileDeviceSupport;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.credit.CreditApplyFacade;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditApplicationService {
    private final CreditApplyFacade creditApplyFacade;
    private final PendanaanProperties pendanaanProperties;

    public CreditApplicationService(CreditApplyFacade creditApplyFacade, PendanaanProperties pendanaanProperties) {
        this.creditApplyFacade = creditApplyFacade;
        this.pendanaanProperties = pendanaanProperties;
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
        LenderDeviceContext device = ProfileDeviceSupport.resolveLenderDevice(
                request.riskDataInfo().openUserDevice(),
                ClientRequestHeaders.require(httpRequest),
                pendanaanProperties
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
                result.riskMinLimit(),
                result.riskMaxLimit(),
                result.psychologicalCreditLimit(),
                result.fakeCreditLimit(),
                result.borrowAmtStepSize()
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
