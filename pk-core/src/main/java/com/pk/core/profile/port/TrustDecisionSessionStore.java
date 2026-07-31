package com.pk.core.profile.port;

import com.pk.core.profile.ocr.TrustDecisionSessionState;
import java.util.Optional;

public interface TrustDecisionSessionStore {
    Optional<TrustDecisionSessionState> find(long userId);

    void save(long userId, TrustDecisionSessionState state);

    void delete(long userId);
}
