package com.pk.core.display;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class DisplayFormatters {
    private static final DecimalFormat IDR_FORMAT = idrFormat();
    private static final DecimalFormat RATE_FORMAT = rateFormat();

    private DisplayFormatters() {
    }

    public static String formatIdrAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal rounded = amount.setScale(0, RoundingMode.HALF_UP);
        return "Rp " + IDR_FORMAT.format(rounded);
    }

    public static String formatComprehensiveRate(BigDecimal rate) {
        if (rate == null) {
            return "";
        }
        BigDecimal percent = rate.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
        return RATE_FORMAT.format(percent) + "% /bulan";
    }

    public static String formatTermNo(int termNo) {
        return "Cicilan ke-" + termNo;
    }

    public static String formatJakartaDate(Long epochMillis) {
        if (epochMillis == null) {
            return "";
        }
        return formatJakartaDate(java.time.Instant.ofEpochMilli(epochMillis));
    }

    public static String formatJakartaDate(java.time.Instant instant) {
        if (instant == null) {
            return "";
        }
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(java.time.ZoneId.of("Asia/Jakarta"))
                .format(instant);
    }

    private static DecimalFormat idrFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,###", symbols);
        format.setGroupingUsed(true);
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format;
    }

    private static DecimalFormat rateFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("0.0", symbols);
        format.setMaximumFractionDigits(1);
        format.setMinimumFractionDigits(1);
        return format;
    }
}
