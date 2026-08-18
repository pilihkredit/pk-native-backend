package com.pk.app.loan.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.credit.dto.request.CreditAppInfoRequest;
import com.pk.app.loan.dto.request.LoanApplyRequest;
import com.pk.app.loan.dto.response.LoanApplyResponse;
import com.pk.app.loan.dto.response.LoanStatusResponse;
import com.pk.app.profile.application.ProfileDeviceSupport;
import com.pk.adapter.apipartner.ApiPartnerProperties;
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

@Service
public class LoanApplyApplicationService {
    private final LoanApplyFacade loanApplyFacade;
    private final ApiPartnerProperties apiPartnerProperties;
    private final UserDeviceWriter userDeviceWriter;

    public LoanApplyApplicationService(
            LoanApplyFacade loanApplyFacade,
            ApiPartnerProperties apiPartnerProperties,
            UserDeviceWriter userDeviceWriter
    ) {
        this.loanApplyFacade = loanApplyFacade;
        this.apiPartnerProperties = apiPartnerProperties;
        this.userDeviceWriter = userDeviceWriter;
    }

    public LoanApplyResponse apply(
            AuthenticatedPrincipal principal,
            LoanApplyRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (!apiPartnerProperties.httpEnabled() || !apiPartnerProperties.httpCredentialsPresent()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        LenderDeviceContext device = ProfileDeviceSupport.resolveRiskLenderDevice(
                request.riskDataInfo().openUserDevice(),
                ClientRequestHeaders.require(httpRequest),
                apiPartnerProperties
        );
        userDeviceWriter.upsertFromRequest(
                principal.userId(),
                principal.partnerUserId(),
                request.requestId(),
                device
        );
        LoanApplyFacade.ApplyResult result = loanApplyFacade.apply(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new LoanApplyFacade.ApplyCommand(
                        request.requestId(),
                        request.applyId(),
                        request.quoteNo(),
                        request.applyAmt(),
                        request.productCode(),
                        request.repayMethod(),
                        request.couponId(),
                        request.loanPurpose(),
                        request.lat(),
                        request.lng(),
                        request.ip(),
                        request.address(),
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
        LoanApplyFacade.StatusResult result = loanApplyFacade.getStatus(principal.userId(), loanApplyId);
        return new LoanStatusResponse(
                result.loanApplyId(),
                result.status(),
                result.applyStatus(),
                result.loanApplyNo(),
                result.billNo(),
                result.applyAmt(),
                result.payAmount(),
                result.payTime(),
                result.freezeEndTime()
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
