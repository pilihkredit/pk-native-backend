package com.pk.app.loan.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.credit.dto.request.CreditAppInfoRequest;
import com.pk.app.loan.dto.request.LoanApplyRequest;
import com.pk.app.loan.dto.response.LoanApplyResponse;
import com.pk.app.loan.dto.response.LoanStatusResponse;
import com.pk.app.profile.application.ProfileDeviceSupport;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.loan.LoanApplyFacade;
import com.pk.infra.profile.UserDeviceWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplyApplicationService {
    private final LoanApplyFacade loanApplyFacade;
    private final PendanaanProperties pendanaanProperties;
    private final UserDeviceWriter userDeviceWriter;

    public LoanApplyApplicationService(
            LoanApplyFacade loanApplyFacade,
            PendanaanProperties pendanaanProperties,
            UserDeviceWriter userDeviceWriter
    ) {
        this.loanApplyFacade = loanApplyFacade;
        this.pendanaanProperties = pendanaanProperties;
        this.userDeviceWriter = userDeviceWriter;
    }

    @Transactional
    public LoanApplyResponse apply(
            AuthenticatedPrincipal principal,
            LoanApplyRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (!pendanaanProperties.httpEnabled() || !pendanaanProperties.httpCredentialsPresent()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        LenderDeviceContext device = ProfileDeviceSupport.resolveRiskLenderDevice(
                request.riskDataInfo().openUserDevice(),
                ClientRequestHeaders.require(httpRequest),
                pendanaanProperties
        );
        userDeviceWriter.upsertFromRequest(
                principal.profileId(),
                principal.partnerUserId(),
                request.loanApplyId(),
                device
        );
        LoanApplyFacade.ApplyResult result = loanApplyFacade.apply(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new LoanApplyFacade.ApplyCommand(
                        request.loanApplyId(),
                        request.quoteNo(),
                        request.loanPurpose(),
                        request.lat(),
                        request.lng(),
                        request.ip(),
                        null,
                        device.adId(),
                        device,
                        toAppList(request.riskDataInfo().appList())
                )
        );
        return new LoanApplyResponse(result.loanApplyId(), result.status(), result.loanApplyNo());
    }

    public LoanStatusResponse getStatus(AuthenticatedPrincipal principal, String loanApplyId) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        LoanApplyFacade.StatusResult result = loanApplyFacade.getStatus(principal.profileId(), loanApplyId);
        return new LoanStatusResponse(
                result.loanApplyId(),
                result.status(),
                result.loanApplyNo(),
                result.billNo(),
                result.applyAmt(),
                result.payAmount(),
                result.payTime()
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
