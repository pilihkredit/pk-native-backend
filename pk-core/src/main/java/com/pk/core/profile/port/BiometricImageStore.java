package com.pk.core.profile.port;

import com.pk.core.profile.BiometricImageKind;

public interface BiometricImageStore {
    /**
     * Store encrypted biometric image under a mobile-number based object key.
     *
     * @param mobileNo account owner mobile number used in the storage path
     */
    String store(String mobileNo, BiometricImageKind kind, byte[] imageBytes);

    byte[] load(String encryptedRef);

    void delete(String encryptedRef);

    String encryptionKeyRef();
}
