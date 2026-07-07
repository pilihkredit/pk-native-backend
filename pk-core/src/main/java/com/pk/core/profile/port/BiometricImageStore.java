package com.pk.core.profile.port;

import com.pk.core.profile.BiometricImageKind;

public interface BiometricImageStore {
    String store(long profileId, BiometricImageKind kind, byte[] imageBytes);

    byte[] load(String encryptedRef);

    void delete(String encryptedRef);

    String encryptionKeyRef();
}
