package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import org.junit.jupiter.api.Test;

class OcrSensitiveJsonSupportTest {
    @Test
    void encryptsIdCardAndMotherNameInPlaceAndStoresImageAsRef() throws Exception {
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.encrypt(any())).thenAnswer(invocation -> {
            String plaintext = invocation.getArgument(0);
            return new EncryptedField("cipher-" + plaintext, new byte[12], new byte[16]);
        });
        BiometricImageStore imageStore = mock(BiometricImageStore.class);
        when(imageStore.store(eq("81234567890"), eq(BiometricImageKind.FACE), any()))
                .thenReturn("enc://face/81234567890");

        OcrSensitiveJsonSupport support = new OcrSensitiveJsonSupport(
                new ObjectMapper(),
                encryptor,
                imageStore
        );

        String input = """
                {
                  "license": "secret-license",
                  "id_card": "3171234567890001",
                  "idNumber": "3603281301870006",
                  "ktpIdNumber": "3603281301870006",
                  "mother_name": "Siti",
                  "ocrName": "Budi",
                  "face_photo_image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
                  "image": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
                }
                """;
        String sanitized = support.sanitizeForStorage(input, "81234567890");
        JsonNode root = new ObjectMapper().readTree(sanitized);

        assertThat(root.path("ocrName").asText()).isEqualTo("Budi");
        assertThat(root.path("id_card").path("enc").asText()).isEqualTo("AES-256-GCM");
        assertThat(root.path("id_card").path("ciphertext").asText()).startsWith("cipher-");
        assertThat(root.path("idNumber").path("enc").asText()).isEqualTo("AES-256-GCM");
        assertThat(root.path("ktpIdNumber").path("enc").asText()).isEqualTo("AES-256-GCM");
        assertThat(root.path("mother_name").path("enc").asText()).isEqualTo("AES-256-GCM");
        assertThat(root.path("license").asText()).isEqualTo("[protected]");
        assertThat(root.path("face_photo_image").path("encryptedRef").asText()).isEqualTo("enc://face/81234567890");
        assertThat(root.path("image").path("encryptedRef").asText()).isEqualTo("enc://face/81234567890");
    }

    @Test
    void encryptsTrustDecisionNikInCardInfo() throws Exception {
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.encrypt(any())).thenAnswer(invocation -> {
            String plaintext = invocation.getArgument(0);
            return new EncryptedField("cipher-" + plaintext, new byte[12], new byte[16]);
        });
        OcrSensitiveJsonSupport support = new OcrSensitiveJsonSupport(
                new ObjectMapper(),
                encryptor,
                mock(BiometricImageStore.class)
        );

        String input = """
                {
                  "code": 200,
                  "message": "success",
                  "card_info": {
                    "nik": "3173040903960004",
                    "name": "KEVIN ACIYANTO",
                    "city": "JAKARTA BARAT"
                  }
                }
                """;
        JsonNode root = new ObjectMapper().readTree(support.sanitizeForStorage(input, "8166478289"));

        assertThat(root.path("card_info").path("nik").path("enc").asText()).isEqualTo("AES-256-GCM");
        assertThat(root.path("card_info").path("nik").path("ciphertext").asText())
                .isEqualTo("cipher-3173040903960004");
        assertThat(root.path("card_info").path("name").asText()).isEqualTo("KEVIN ACIYANTO");
        assertThat(root.path("card_info").path("city").asText()).isEqualTo("JAKARTA BARAT");
    }
}
