package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Field-level encryption for persisted OCR/identity JSON payloads.
 * Compliant fields from 「数据存储分类与合规建议」:
 * id_card, mother_name must be AES-256-GCM; images use BiometricImageStore refs.
 */
public class OcrSensitiveJsonSupport {
    private static final Set<String> TEXT_ENCRYPT_FIELDS = Set.of(
            "id_card",
            "idcard",
            "idno",
            "idnumber",
            "ocridno",
            "ktpidnumber",
            "id_card_number",
            "idcardnumber",
            "mother_name",
            "mothername",
            "mothersurname"
    );

    private static final Set<String> IMAGE_FIELDS = Set.of(
            "id_card_image",
            "idcardimage",
            "face_photo_image",
            "facephotoimage",
            "ocrimage",
            "firstimage",
            "secondimage",
            "faceimagebase64",
            "idcardimagebase64",
            "facebase64",
            "idcardbase64",
            "detectionresult",
            "imagebase64"
    );

    private final ObjectMapper objectMapper;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final BiometricImageStore biometricImageStore;

    public OcrSensitiveJsonSupport(
            ObjectMapper objectMapper,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            BiometricImageStore biometricImageStore
    ) {
        this.objectMapper = objectMapper;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.biometricImageStore = biometricImageStore;
    }

    public String sanitizeForStorage(String json, String mobileNo) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            sanitizeNode(root, mobileNo);
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            return json;
        }
    }

    public String sanitizeMapForStorage(Map<String, ?> requestData, String mobileNo) {
        try {
            return sanitizeForStorage(objectMapper.writeValueAsString(requestData), mobileNo);
        } catch (Exception exception) {
            return "{}";
        }
    }

    public String describeMultipartRequest(Map<String, byte[]> form, String mobileNo) {
        ObjectNode node = objectMapper.createObjectNode();
        if (form == null) {
            return node.toString();
        }
        for (Map.Entry<String, byte[]> entry : form.entrySet()) {
            String key = entry.getKey();
            byte[] bytes = entry.getValue() == null ? new byte[0] : entry.getValue();
            ObjectNode field = objectMapper.createObjectNode();
            field.put("bytes", bytes.length);
            if (isImageField(key) && hasMobile(mobileNo) && bytes.length > 0) {
                try {
                    BiometricImageKind kind = resolveImageKind(key);
                    String ref = biometricImageStore.store(mobileNo, kind, bytes);
                    field.put("encryptedRef", ref);
                } catch (Exception ignored) {
                    field.put("encryptedRef", "store-failed");
                }
            }
            node.set(key, field);
        }
        return node.toString();
    }

    private void sanitizeNode(JsonNode node, String mobileNo) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fields = objectNode.fieldNames();
            // Collect first to avoid ConcurrentModification
            java.util.List<String> names = new java.util.ArrayList<>();
            fields.forEachRemaining(names::add);
            for (String field : names) {
                JsonNode child = objectNode.get(field);
                if (child != null && child.isTextual()) {
                    String value = child.asText();
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    if (isTextEncryptField(field)) {
                        objectNode.set(field, encryptTextNode(value));
                    } else if (isImageField(field) && looksLikeBase64Image(value)) {
                        objectNode.set(field, encryptOrStoreImage(value, field, mobileNo));
                    }
                } else {
                    sanitizeNode(child, mobileNo);
                }
            }
            return;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode child : arrayNode) {
                sanitizeNode(child, mobileNo);
            }
        }
    }

    private ObjectNode encryptTextNode(String plaintext) {
        EncryptedField encrypted = sensitiveFieldEncryptor.encrypt(plaintext);
        ObjectNode node = objectMapper.createObjectNode();
        node.put("enc", "AES-256-GCM");
        node.put("ciphertext", encrypted.ciphertextBase64());
        node.put("nonce", Base64.getEncoder().encodeToString(encrypted.nonce()));
        node.put("tag", Base64.getEncoder().encodeToString(encrypted.tag()));
        return node;
    }

    private JsonNode encryptOrStoreImage(String base64, String field, String mobileNo) {
        if (hasMobile(mobileNo)) {
            try {
                byte[] bytes = OcrImageSupport.decodeBase64Image(base64, Integer.MAX_VALUE);
                String ref = biometricImageStore.store(mobileNo, resolveImageKind(field), bytes);
                ObjectNode node = objectMapper.createObjectNode();
                node.put("encryptedRef", ref);
                return node;
            } catch (Exception ignored) {
                // fall through to field encryption
            }
        }
        return encryptTextNode(base64);
    }

    private static boolean hasMobile(String mobileNo) {
        return mobileNo != null && !mobileNo.isBlank();
    }

    private static boolean isTextEncryptField(String field) {
        return TEXT_ENCRYPT_FIELDS.contains(normalize(field));
    }

    private static boolean isImageField(String field) {
        return IMAGE_FIELDS.contains(normalize(field));
    }

    private static String normalize(String field) {
        return field == null ? "" : field.replace("_", "").toLowerCase(Locale.ROOT);
    }

    private static BiometricImageKind resolveImageKind(String field) {
        String normalized = normalize(field);
        if (normalized.contains("face") || normalized.contains("second") || normalized.contains("detection")) {
            return BiometricImageKind.FACE;
        }
        return BiometricImageKind.ID_CARD;
    }

    private static boolean looksLikeBase64Image(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("data:image")) {
            return true;
        }
        if (trimmed.length() < 16) {
            return false;
        }
        return trimmed.matches("^[A-Za-z0-9+/=\\r\\n]+$");
    }
}
