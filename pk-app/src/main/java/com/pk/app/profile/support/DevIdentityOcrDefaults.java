package com.pk.app.profile.support;

public final class DevIdentityOcrDefaults {
    public static final String OCR_NAME = "STEFANUS NURYANTO";
    public static final String OCR_ID_NO = "3603281301870006";
    public static final String GENDER = "LAKI-LAKI";
    public static final String RELIGION = "KATHOLIK";
    public static final String MARITAL_STATUS = "KAWIN";
    public static final String BIRTHDAY = "1987-01-13";
    public static final String BIRTH_PLACE = "JAKARTA";
    public static final String ADDRESS = "BANJARSARI";
    public static final String OCCUPATION = "KARYAWAN SWASTA";
    public static final String NATIONALITY = "WNI";
    public static final String BLOOD_TYPE = "O";
    public static final String EXPIRY_DATE = "SEUMUR HIDUP";
    public static final String PROVINCE = "DAERAH ISTIMEWA YOGYAKARTA";
    public static final String CITY = "KABUPATEN SLEMAN";
    public static final String DISTRICT = "PAKEM";

    private DevIdentityOcrDefaults() {
    }

    public static String orDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
