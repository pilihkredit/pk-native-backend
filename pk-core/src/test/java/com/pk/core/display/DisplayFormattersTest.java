package com.pk.core.display;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DisplayFormattersTest {
    @Test
    void formatsIdrAmount() {
        assertEquals("Rp 1.500.000", DisplayFormatters.formatIdrAmount(new BigDecimal("1500000")));
    }

    @Test
    void formatsComprehensiveRate() {
        assertEquals("18,0% /bulan", DisplayFormatters.formatComprehensiveRate(new BigDecimal("0.18")));
    }
}
