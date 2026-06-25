package com.pk.infra.disclosure;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.disclosure.DisclosureProperties.LocaleContent;
import com.pk.infra.disclosure.DisclosureProperties.SceneConfig;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DisclosureFacade {
    static final String LOCALE_ID = "id-ID";
    static final String LOCALE_EN = "en";
    private static final Set<String> SUPPORTED_SCENES = Set.of("APP_LAUNCH", "BEFORE_DEVICE");

    private final DisclosureProperties disclosureProperties;

    public DisclosureFacade(DisclosureProperties disclosureProperties) {
        this.disclosureProperties = disclosureProperties;
    }

    public PermissionResult getPermission(String scene, String acceptLanguage) {
        String resolvedScene = scene == null || scene.isBlank()
                ? DisclosureProperties.DEFAULT_SCENE
                : scene.trim();
        if (!SUPPORTED_SCENES.contains(resolvedScene)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        SceneConfig sceneConfig = disclosureProperties.scene(resolvedScene);
        if (sceneConfig == null) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }

        String locale = resolveLocale(acceptLanguage);
        LocaleContent content = resolveLocaleContent(sceneConfig.locales(), locale);
        if (content == null) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }

        return new PermissionResult(
                sceneConfig.disclosureId(),
                sceneConfig.version(),
                locale,
                List.copyOf(content.bodyParagraphs()),
                content.agreeButtonText(),
                content.disagreeButtonText(),
                sceneConfig.mustAgree(),
                sceneConfig.iconType(),
                sceneConfig.updatedAt()
        );
    }

    static String resolveLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return LOCALE_ID;
        }
        List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLanguage);
        for (Locale.LanguageRange range : ranges) {
            String tag = range.getRange().toLowerCase(Locale.ROOT);
            if (tag.startsWith("id")) {
                return LOCALE_ID;
            }
            if (tag.startsWith("en")) {
                return LOCALE_EN;
            }
        }
        return LOCALE_ID;
    }

    private static LocaleContent resolveLocaleContent(Map<String, LocaleContent> locales, String locale) {
        LocaleContent exact = locales.get(locale);
        if (exact != null) {
            return exact;
        }
        if (LOCALE_EN.equals(locale)) {
            return locales.get(LOCALE_EN);
        }
        return locales.get(LOCALE_ID);
    }

    public record PermissionResult(
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
