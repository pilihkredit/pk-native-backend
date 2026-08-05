package com.pk.app.review.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.review.application.ReviewGuideApplicationService;
import com.pk.app.review.dto.request.ReviewGuideClaimRequest;
import com.pk.app.review.dto.request.ReviewGuideFeedbackRequest;
import com.pk.app.review.dto.response.ReviewGuideClaimResponse;
import com.pk.app.security.SecurityContextSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review-guide")
public class ReviewGuideController {
    private final ReviewGuideApplicationService reviewGuideApplicationService;

    public ReviewGuideController(ReviewGuideApplicationService reviewGuideApplicationService) {
        this.reviewGuideApplicationService = reviewGuideApplicationService;
    }

    @PostMapping("/claim")
    public ApiResponse<ReviewGuideClaimResponse> claim(
            @Valid @RequestBody ReviewGuideClaimRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                reviewGuideApplicationService.claim(SecurityContextSupport.requirePrincipal(), request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/feedback")
    public ApiResponse<Void> feedback(
            @Valid @RequestBody ReviewGuideFeedbackRequest request,
            HttpServletRequest httpRequest
    ) {
        reviewGuideApplicationService.feedback(SecurityContextSupport.requirePrincipal(), request);
        return ApiResponse.success(null, RequestTrace.resolveTraceId(httpRequest));
    }
}
