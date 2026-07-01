package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.profile.ocr.OcrSessionState;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class OcrFieldParser {
    private static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");

    private OcrFieldParser() {
    }

    public static OcrSessionState.OcrParsedFields parse(JsonNode data) {
        if (data == null || data.isNull() || data.isEmpty()) {
            return null;
        }
        String ocrName = text(data, "name");
        String ocrIdNo = firstText(data, "ktpIdNumber", "idNumber");
        String birthday = parseBirthday(data, ocrIdNo);
        String birthPlace = parseBirthPlace(data);
        return new OcrSessionState.OcrParsedFields(
                ocrName,
                ocrIdNo,
                readTextOrNumber(data, "gender"),
                readTextOrNumber(data, "religion"),
                readMaritalStatus(data),
                birthday,
                birthPlace,
                text(data, "address"),
                text(data, "occupation"),
                text(data, "nationality"),
                text(data, "bloodType"),
                text(data, "expiryDate"),
                text(data, "province"),
                text(data, "city"),
                text(data, "district")
        );
    }

    private static String parseBirthday(JsonNode data, String ocrIdNo) {
        if (data.hasNonNull("dateOfBirth")) {
            try {
                long epochSeconds = data.get("dateOfBirth").asLong();
                if (epochSeconds > 0) {
                    LocalDate date = Instant.ofEpochSecond(epochSeconds).atZone(JAKARTA).toLocalDate();
                    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        String textBirthday = text(data, "birthday");
        if (textBirthday != null && !textBirthday.isBlank()) {
            return normalizeBirthday(textBirthday.trim());
        }
        return parseBirthdayFromIdNo(ocrIdNo);
    }

    private static String parseBirthPlace(JsonNode data) {
        String birthPlace = firstText(data, "birthPlace", "placeOfBirth");
        if (birthPlace != null) {
            return birthPlace;
        }
        String birthPlaceBirthday = text(data, "birthPlaceBirthday");
        if (birthPlaceBirthday == null || !birthPlaceBirthday.contains(",")) {
            return null;
        }
        return birthPlaceBirthday.substring(0, birthPlaceBirthday.indexOf(',')).trim();
    }

    private static String normalizeBirthday(String birthday) {
        if (birthday.contains("/")) {
            return birthday.replace('/', '-');
        }
        if (birthday.matches("\\d{2}-\\d{2}-\\d{4}")) {
            String[] parts = birthday.split("-");
            return String.format("%s-%s-%s", parts[2], parts[1], parts[0]);
        }
        return birthday;
    }

    private static String parseBirthdayFromIdNo(String ocrIdNo) {
        if (ocrIdNo == null || ocrIdNo.length() < 12) {
            return null;
        }
        try {
            String dob = ocrIdNo.substring(6, 12);
            int day = Integer.parseInt(dob.substring(0, 2));
            int month = Integer.parseInt(dob.substring(2, 4));
            int year = Integer.parseInt(dob.substring(4, 6));
            if (day > 40) {
                day -= 40;
            }
            int currentYear = LocalDate.now(JAKARTA).getYear() % 100;
            int fullYear = year > currentYear ? 1900 + year : 2000 + year;
            return String.format("%04d-%02d-%02d", fullYear, month, day);
        } catch (Exception exception) {
            return null;
        }
    }

    private static String readMaritalStatus(JsonNode data) {
        String maritalStatus = text(data, "maritalStatus");
        if (maritalStatus != null) {
            return maritalStatus;
        }
        return readTextOrNumber(data, "marital");
    }

    private static String readTextOrNumber(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asText();
        }
        return text(node, field);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text.trim();
    }

    private static String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
