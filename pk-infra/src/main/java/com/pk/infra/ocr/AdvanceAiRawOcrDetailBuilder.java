package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.ocr.OcrSessionState;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Builds Advance.ai OCR {@code data} JSON for lender {@code ocrResult.rawOcrDetail}.
 * Field names must match Advance.ai raw response, not PK-normalized OCR fields.
 */
public final class AdvanceAiRawOcrDetailBuilder {
    private static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter BIRTH_PLACE_BIRTHDAY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private AdvanceAiRawOcrDetailBuilder() {
    }

    public static String build(ObjectMapper objectMapper, OcrSessionState.OcrParsedFields parsed) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            putIfPresent(node, "name", parsed.ocrName());
            putIfPresent(node, "ktpIdNumber", parsed.ocrIdNo());
            putIfPresent(node, "gender", parsed.gender());
            putIfPresent(node, "religion", parsed.religion());
            putIfPresent(node, "maritalStatus", parsed.maritalStatus());
            putIfPresent(node, "address", parsed.address());
            putIfPresent(node, "occupation", parsed.occupation());
            putIfPresent(node, "nationality", parsed.nationality());
            putIfPresent(node, "bloodType", parsed.bloodType());
            putIfPresent(node, "expiryDate", parsed.expiryDate());
            putIfPresent(node, "province", parsed.province());
            putIfPresent(node, "city", parsed.city());
            putIfPresent(node, "district", parsed.district());
            putIfPresent(node, "birthPlace", parsed.birthPlace());
            putIfPresent(node, "placeOfBirth", parsed.birthPlace());
            putBirthFields(node, parsed.birthday(), parsed.birthPlace());
            return objectMapper.writeValueAsString(node);
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private static void putBirthFields(ObjectNode node, String birthday, String birthPlace) {
        if (birthday == null || birthday.isBlank()) {
            return;
        }
        LocalDate birthDate = parseBirthday(birthday.trim());
        if (birthDate == null) {
            return;
        }
        long epochSeconds = birthDate.atStartOfDay(JAKARTA).toEpochSecond();
        node.put("dateOfBirth", epochSeconds);
        if (birthPlace != null && !birthPlace.isBlank()) {
            String birthPlaceBirthday = birthPlace.trim() + ", " + birthDate.format(BIRTH_PLACE_BIRTHDAY);
            node.put("birthPlaceBirthday", birthPlaceBirthday);
        }
    }

    private static LocalDate parseBirthday(String birthday) {
        try {
            return LocalDate.parse(birthday, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        if (birthday.matches("\\d{2}-\\d{2}-\\d{4}")) {
            String[] parts = birthday.split("-");
            try {
                return LocalDate.of(
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[0])
                );
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static void putIfPresent(ObjectNode node, String field, String value) {
        if (value != null && !value.isBlank()) {
            node.put(field, value.trim());
        }
    }
}
