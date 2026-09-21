package com.pk.app.ops.support;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OpsAccountClosureUserResolver {
    private final UserAuthRepository userAuthRepository;

    public OpsAccountClosureUserResolver(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    public UserProfileSummary resolveActiveUser(Long userId, String mobileNo, String partnerUserId) {
        Optional<UserProfileSummary> resolved = Optional.empty();
        if (userId != null && userId > 0) {
            resolved = userAuthRepository.findByUserId(userId);
        } else if (mobileNo != null && !mobileNo.isBlank()) {
            resolved = userAuthRepository.findByMobileNo(mobileNo.trim());
        } else if (partnerUserId != null && !partnerUserId.isBlank()) {
            resolved = userAuthRepository.findByPartnerUserId(partnerUserId.trim());
        } else {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "One of userId, mobileNo, or partnerUserId is required"
            );
        }
        return resolved.orElseThrow(() -> new ApiException(
                ApiCode.INVALID_REQUEST_PARAMETERS,
                "Active user not found"
        ));
    }
}
