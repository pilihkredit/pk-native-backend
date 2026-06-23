package com.pk.core.auth.port;

import com.pk.core.auth.UserProfileSummary;
import java.util.Optional;

public interface UserAuthRepository {
    Optional<UserProfileSummary> findByMobileNo(String mobileNo);

    Optional<UserProfileSummary> findByProfileId(long profileId);

    UserProfileSummary createByMobileNo(String mobileNo);
}
