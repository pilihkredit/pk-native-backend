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

    @Test
    void storesVersionedImagesWithoutOverwritingEarlierCapture() {
        BiometricImageStore store = createStore(tempDir);
        byte[] first = new byte[] {1, 2, 3};
        byte[] second = new byte[] {4, 5, 6};

        String firstRef = store.storeVersioned(
                "81234567890", BiometricImageKind.MOBILE_CHANGE_FACE, "face-token-1", first);
        String secondRef = store.storeVersioned(
                "81234567890", BiometricImageKind.MOBILE_CHANGE_FACE, "face-token-2", second);

        assertThat(firstRef).isNotEqualTo(secondRef);
        assertThat(store.load(firstRef)).isEqualTo(first);
        assertThat(store.load(secondRef)).isEqualTo(second);
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
