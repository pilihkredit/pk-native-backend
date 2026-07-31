package com.pk.infra.disclosure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.app.disclosure")
public class DisclosureProperties {
    public static final String DEFAULT_SCENE = "APP_LAUNCH";

    private Map<String, SceneConfig> scenes = new LinkedHashMap<>();

    public Map<String, SceneConfig> scenes() {
        return scenes;
    }

    public void setScenes(Map<String, SceneConfig> scenes) {
        this.scenes = scenes == null ? new LinkedHashMap<>() : scenes;
    }

    public SceneConfig scene(String scene) {
        return scenes.get(scene);
    }

    public static class SceneConfig {
        private String disclosureId = "";
        private String version = "";
        private long updatedAt;
        private boolean mustAgree = true;
        private String iconType = "WARNING";
        private Map<String, LocaleContent> locales = new LinkedHashMap<>();

        public String disclosureId() {
            return disclosureId;
        }

        public void setDisclosureId(String disclosureId) {
            this.disclosureId = disclosureId;
        }

        public String version() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public long updatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
        }

        public boolean mustAgree() {
            return mustAgree;
        }

        public void setMustAgree(boolean mustAgree) {
            this.mustAgree = mustAgree;
        }

        public String iconType() {
            return iconType;
        }

        public void setIconType(String iconType) {
            this.iconType = iconType;
        }

        public Map<String, LocaleContent> locales() {
            return locales;
        }

        public void setLocales(Map<String, LocaleContent> locales) {
            this.locales = locales == null ? new LinkedHashMap<>() : locales;
        }
    }

    public static class LocaleContent {
        private List<String> bodyParagraphs = List.of();
        private String agreeButtonText = "";
        private String disagreeButtonText = "";

        public List<String> bodyParagraphs() {
            return bodyParagraphs;
        }

        public void setBodyParagraphs(List<String> bodyParagraphs) {
            this.bodyParagraphs = bodyParagraphs == null ? List.of() : List.copyOf(bodyParagraphs);
        }

        public String agreeButtonText() {
            return agreeButtonText;
        }

        public void setAgreeButtonText(String agreeButtonText) {
            this.agreeButtonText = agreeButtonText;
        }

        public String disagreeButtonText() {
            return disagreeButtonText;
        }

        public void setDisagreeButtonText(String disagreeButtonText) {
            this.disagreeButtonText = disagreeButtonText;
        }
    }
}
