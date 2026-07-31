package com.pk.app.agreement.controller;

import com.pk.app.agreement.application.AgreementApplicationService;
import com.pk.app.agreement.application.AgreementFieldApplicationService;
import com.pk.app.agreement.dto.request.AgreementCreateRequest;
import com.pk.app.agreement.dto.request.AgreementLatestRequest;
import com.pk.app.agreement.dto.response.AgreementCreateResponse;
import com.pk.app.agreement.dto.response.AgreementLatestResponse;
import com.pk.app.agreement.dto.response.LoanAgreementFieldsResponse;
import com.pk.app.agreement.dto.response.ProductSummaryFieldsResponse;
import com.pk.app.agreement.dto.response.StatementFieldsResponse;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agreement")
public class AgreementController {
    private final AgreementApplicationService agreementApplicationService;
    private final AgreementFieldApplicationService agreementFieldApplicationService;

    public AgreementController(
            AgreementApplicationService agreementApplicationService,
            AgreementFieldApplicationService agreementFieldApplicationService
    ) {
        this.agreementApplicationService = agreementApplicationService;
        this.agreementFieldApplicationService = agreementFieldApplicationService;
    }

    @GetMapping("/product-summary/fields")
    public ApiResponse<ProductSummaryFieldsResponse> productSummaryFields(HttpServletRequest httpRequest) {
        return ApiResponse.success(
                agreementFieldApplicationService.productSummary(SecurityContextSupport.requirePrincipal()),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/statement/fields")
    public ApiResponse<StatementFieldsResponse> statementFields(HttpServletRequest httpRequest) {
        return ApiResponse.success(
                agreementFieldApplicationService.statement(SecurityContextSupport.requirePrincipal()),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/loan/fields")
    public ApiResponse<LoanAgreementFieldsResponse> loanFields(
            @RequestParam("quoteNo") String quoteNo,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                agreementFieldApplicationService.loanPreview(
                        SecurityContextSupport.requirePrincipal(),
                        quoteNo
                ),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Append agreement records. Logged-in: mobile/partner from token. Anonymous: neither required. */
    @PublicApi
    @PostMapping("/records")
    public ApiResponse<AgreementCreateResponse> create(
            @Valid @RequestBody AgreementCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                agreementApplicationService.create(SecurityContextSupport.requirePrincipal(), request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Query latest agreement status by type for a mobile number. */
    @PublicApi
    @PostMapping("/latest")
    public ApiResponse<AgreementLatestResponse> latest(
            @Valid @RequestBody AgreementLatestRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                agreementApplicationService.latest(request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
