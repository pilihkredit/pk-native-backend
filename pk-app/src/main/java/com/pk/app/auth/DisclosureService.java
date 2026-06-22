package com.pk.app.auth;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DisclosureService {
    public DisclosureConfig getPermissionDisclosure(String scene, String acceptLanguage) {
        if (scene != null && !scene.isBlank()
                && !"APP_LAUNCH".equals(scene)
                && !"BEFORE_DEVICE".equals(scene)) {
            throw new com.pk.core.error.AppBusinessException(com.pk.core.error.AppErrorCodes.INVALID_REQUEST);
        }
        String locale = resolveLocale(acceptLanguage);
        return new DisclosureConfig(
                "permission_disclosure_id_v1",
                "1.0.0",
                locale,
                List.of(
                        "Harap diketahui demi penilaian kualifikasi Anda yang lebih baik untuk produk keuangan kami, "
                                + "kami membutuhkan akses informasi di bawah ini untuk menjalankan penilaian resiko dan "
                                + "perhitungan skor pinjaman Anda. Seluruh informasi akan dikumpulkan dan dikirim dalam "
                                + "teknologi keamanan HTTPS yang terbaru, dan hanya digunakan untuk penilaian nilai pinjaman "
                                + "/ cicilan pinjaman Anda. Kami tidak akan membagikan data Anda dengan pihak ketiga yang "
                                + "tidak berkaitan dengan tujuan permohonan kecuali perintah pemerintah.",
                        "Berdasarkan hukum negara Republik Indonesia dan regulasi Google, kami akan menghormati dan "
                                + "melindungi privasi Anda."
                ),
                "Saya mengerti dan setuju",
                "Saya Tidak Setuju",
                true,
                "WARNING",
                1_750_600_000_000L
        );
    }

    private static String resolveLocale(String acceptLanguage) {
        if (acceptLanguage != null && acceptLanguage.toLowerCase().startsWith("id")) {
            return "id-ID";
        }
        return "id-ID";
    }

    public record DisclosureConfig(
            String disclosureId,
            String version,
            String locale,
            List<String> bodyParagraphs,
            String agreeButtonText,
            String disagreeButtonText,
            boolean mustAgree,
            String iconType,
            long updatedAt
    ) {
    }
}
