package com.pk.app.debug.application;

import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.app.debug.dto.DebugUserProgressResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.debug.mapper.DebugUserProgressReadMapper;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DebugUserProgressApplicationService {
    private static final int INTERACTION_LIMIT = 80;

    private final UserAuthRepository userAuthRepository;
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final DebugUserProgressReadMapper readMapper;
    private final DebugUserProgressProperties properties;

    public DebugUserProgressApplicationService(
            UserAuthRepository userAuthRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            DebugUserProgressReadMapper readMapper,
            DebugUserProgressProperties properties
    ) {
        this.userAuthRepository = userAuthRepository;
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.readMapper = readMapper;
        this.properties = properties;
    }

    public DebugUserProgressResponse query(String debugToken, String mobileNo) {
        validateToken(debugToken);
        if (mobileNo == null || mobileNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return userAuthRepository.findByMobileNo(mobileNo.trim())
                .map(this::buildResponse)
                .orElseGet(DebugUserProgressResponse::notFound);
    }

    private DebugUserProgressResponse buildResponse(UserProfileSummary user) {
        OnboardingProgressFacade.OnboardingProgressResult progress =
                onboardingProgressFacade.getProgress(user.profileId(), user.partnerUserId());
        List<String> creditApplyIds = readMapper.findCreditApplyIds(user.profileId());
        List<String> loanApplyIds = readMapper.findLoanApplyIds(user.profileId());
        List<String> businessIds = businessIds(user, creditApplyIds, loanApplyIds);
        List<DebugUserProgressResponse.InteractionInfo> interactions = readMapper
                .findRecentInteractions(businessIds, user.partnerUserId(), user.mobileNo(), INTERACTION_LIMIT)
                .stream()
                .map(DebugUserProgressApplicationService::toInteractionInfo)
                .toList();
        return new DebugUserProgressResponse(
                true,
                new DebugUserProgressResponse.UserInfo(user.profileId(), user.partnerUserId(), user.mobileNo()),
                new DebugUserProgressResponse.ProgressInfo(
                        progress.kycStatus(),
                        progress.completedModules(),
                        progress.missingModules()
                ),
                interactions
        );
    }

    private void validateToken(String debugToken) {
        if (!properties.enabled() || !properties.tokenConfigured()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        if (debugToken == null || !properties.token().equals(debugToken.trim())) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }

    private static List<String> businessIds(
            UserProfileSummary user,
            List<String> creditApplyIds,
            List<String> loanApplyIds
    ) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        addIfPresent(ids, user.partnerUserId());
        addIfPresent(ids, user.mobileNo());
        creditApplyIds.forEach(id -> addIfPresent(ids, id));
        loanApplyIds.forEach(id -> addIfPresent(ids, id));
        return new ArrayList<>(ids);
    }

    private static void addIfPresent(LinkedHashSet<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value.trim());
        }
    }

    private static DebugUserProgressResponse.InteractionInfo toInteractionInfo(
            DebugUserProgressReadMapper.InteractionRecord record
    ) {
        return new DebugUserProgressResponse.InteractionInfo(
                record.id(),
                record.providerCode(),
                record.interactionNo(),
                record.businessType(),
                record.businessId(),
                record.httpMethod(),
                record.endpoint(),
                record.responseCode(),
                record.responseMsg(),
                record.success(),
                record.durationMs(),
                record.requestRef(),
                record.responseRef(),
                record.createdAt()
        );
    }
}
