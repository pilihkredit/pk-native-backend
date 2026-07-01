package com.pk.core.profile.port;

import com.pk.core.profile.ocr.OcrSessionState;
import java.util.Optional;

public interface OcrSessionStore {
    Optional<OcrSessionState> find(long profileId);

    void save(long profileId, OcrSessionState state);
}
