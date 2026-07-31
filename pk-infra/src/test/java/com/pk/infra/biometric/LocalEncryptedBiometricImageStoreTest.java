package com.pk.infra.biometric;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.infra.profile.AesGcmSensitiveFieldEncryptor;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalEncryptedBiometricImageStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void storesAndLoadsEncryptedImageWithoutPlaintextOnDisk() throws Exception {
        BiometricImageStore store = createStore(tempDir);
        byte[] original = new byte[] {(byte) 0xFF, 0x00, 0x10, 0x20, 0x30};

        String ref = store.store("81234567890", BiometricImageKind.ID_CARD, original);
        byte[] restored = store.load(ref);

        assertThat(ref).startsWith("local://");
        assertThat(restored).isEqualTo(original);
        assertThat(Files.readAllBytes(tempDir.resolve("pk-biometric/test/mobile/81234567890/id-card.enc")))
                .isNotEqualTo(original);
    }

    @Test
    void deletesEncryptedImage() {
        BiometricImageStore store = createStore(tempDir);
        String ref = store.store("81234567890", BiometricImageKind.FACE, new byte[] {1, 2, 3});

        store.delete(ref);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> store.load(ref))
                .isInstanceOf(IllegalStateException.class);
    }

    private static BiometricImageStore createStore(Path tempDir) {
        BiometricStorageProperties properties = new BiometricStorageProperties();
        properties.local().setBaseDir(tempDir.toString());
        return new LocalEncryptedBiometricImageStore(
                properties,
                new AesGcmSensitiveFieldEncryptor("test-biometric-encryption-key"),
                "test"
        );
    }
}
