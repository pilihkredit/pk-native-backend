package com.pk.infra.disclosure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.disclosure.DisclosureProperties.LocaleContent;
import com.pk.infra.disclosure.DisclosureProperties.SceneConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DisclosureFacadeTest {
    private DisclosureFacade facade;

    @BeforeEach
    void setUp() {
        DisclosureProperties properties = new DisclosureProperties();
        properties.setScenes(sampleScenes());
        facade = new DisclosureFacade(properties);
    }

    @Test
    void returnsIndonesianContentByDefault() {
        DisclosureFacade.PermissionResult result = facade.getPermission(null, null);

        assertThat(result.locale()).isEqualTo(DisclosureFacade.LOCALE_ID);
        assertThat(result.disclosureId()).isEqualTo("permission_disclosure_id_v1");
        assertThat(result.bodyParagraphs()).hasSize(2);
        assertThat(result.agreeButtonText()).isEqualTo("Saya mengerti dan setuju");
        assertThat(result.mustAgree()).isTrue();
        assertThat(result.iconType()).isEqualTo("WARNING");
    }

    @Test
    void resolvesEnglishFromAcceptLanguage() {
        DisclosureFacade.PermissionResult result = facade.getPermission("APP_LAUNCH", "en-US,en;q=0.9");

        assertThat(result.locale()).isEqualTo(DisclosureFacade.LOCALE_EN);
        assertThat(result.agreeButtonText()).isEqualTo("I understand and agree");
    }

    @Test
    void resolvesSceneBeforeDevice() {
        DisclosureFacade.PermissionResult result = facade.getPermission("BEFORE_DEVICE", "id-ID");

        assertThat(result.disclosureId()).isEqualTo("permission_disclosure_before_device_v1");
        assertThat(result.bodyParagraphs()).hasSize(1);
    }

    @Test
    void rejectsInvalidScene() {
        assertThatThrownBy(() -> facade.getPermission("INVALID", "id-ID"))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void failsWhenSceneConfigMissing() {
        DisclosureProperties properties = new DisclosureProperties();
        properties.setScenes(Map.of());
        DisclosureFacade emptyFacade = new DisclosureFacade(properties);

        assertThatThrownBy(() -> emptyFacade.getPermission("APP_LAUNCH", "id-ID"))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);
    }

    @Test
    void resolveLocalePrefersIndonesianTag() {
        assertThat(DisclosureFacade.resolveLocale("id-ID,en;q=0.8"))
                .isEqualTo(DisclosureFacade.LOCALE_ID);
    }

    private static Map<String, SceneConfig> sampleScenes() {
        Map<String, SceneConfig> scenes = new LinkedHashMap<>();

        SceneConfig appLaunch = new SceneConfig();
        appLaunch.setDisclosureId("permission_disclosure_id_v1");
        appLaunch.setVersion("1.0.0");
        appLaunch.setUpdatedAt(1_750_600_000_000L);
        appLaunch.setMustAgree(true);
        appLaunch.setIconType("WARNING");
        appLaunch.setLocales(Map.of(
                DisclosureFacade.LOCALE_ID, idLocale(),
                DisclosureFacade.LOCALE_EN, enLocale()
        ));
        scenes.put("APP_LAUNCH", appLaunch);

        SceneConfig beforeDevice = new SceneConfig();
        beforeDevice.setDisclosureId("permission_disclosure_before_device_v1");
        beforeDevice.setVersion("1.0.0");
        beforeDevice.setUpdatedAt(1_750_600_000_000L);
        beforeDevice.setMustAgree(true);
        beforeDevice.setIconType("WARNING");
        beforeDevice.setLocales(Map.of(
                DisclosureFacade.LOCALE_ID, beforeDeviceIdLocale()
        ));
        scenes.put("BEFORE_DEVICE", beforeDevice);

        return scenes;
    }

    private static LocaleContent idLocale() {
        LocaleContent content = new LocaleContent();
        content.setBodyParagraphs(List.of(
                "Paragraf pertama.",
                "Paragraf kedua."
        ));
        content.setAgreeButtonText("Saya mengerti dan setuju");
        content.setDisagreeButtonText("Saya Tidak Setuju");
        return content;
    }

    private static LocaleContent enLocale() {
        LocaleContent content = new LocaleContent();
        content.setBodyParagraphs(List.of("First paragraph.", "Second paragraph."));
        content.setAgreeButtonText("I understand and agree");
        content.setDisagreeButtonText("I disagree");
        return content;
    }

    private static LocaleContent beforeDeviceIdLocale() {
        LocaleContent content = new LocaleContent();
        content.setBodyParagraphs(List.of(
                "Sebelum mengumpulkan informasi perangkat, kami memerlukan persetujuan Anda."
        ));
        content.setAgreeButtonText("Saya mengerti dan setuju");
        content.setDisagreeButtonText("Saya Tidak Setuju");
        return content;
    }
}
