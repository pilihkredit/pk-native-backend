package com.pk.infra.biometric;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "pk.biometric-storage")
public class BiometricStorageProperties {
    private String encryptionKeyRef = "pk-field-encryption-key";

    @NestedConfigurationProperty
    private Oss oss = new Oss();

    @NestedConfigurationProperty
    private Local local = new Local();

    public String getEncryptionKeyRef() {
        return encryptionKeyRef;
    }

    public void setEncryptionKeyRef(String encryptionKeyRef) {
        this.encryptionKeyRef = encryptionKeyRef;
    }

    public String encryptionKeyRef() {
        return encryptionKeyRef;
    }

    public Oss getOss() {
        return oss;
    }

    public void setOss(Oss oss) {
        this.oss = oss;
    }

    public Oss oss() {
        return oss;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Local local() {
        return local;
    }

    public static class Oss {
        private boolean enabled = false;
        private String endpoint = "";
        private String bucket = "";
        private String accessKeyId = "";
        private String accessKeySecret = "";
        private String pathPrefix = "pk-biometric";

        public boolean isEnabled() {
            return enabled;
        }

        public boolean enabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String endpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucket() {
            return bucket;
        }

        public String bucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public String accessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public String accessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getPathPrefix() {
            return pathPrefix;
        }

        public String pathPrefix() {
            return pathPrefix;
        }

        public void setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }
    }

    public static class Local {
        private String baseDir = System.getProperty("java.io.tmpdir") + "/pk-biometric";
        private String pathPrefix = "pk-biometric";

        public String getBaseDir() {
            return baseDir;
        }

        public String baseDir() {
            return baseDir;
        }

        public void setBaseDir(String baseDir) {
            this.baseDir = baseDir;
        }

        public String getPathPrefix() {
            return pathPrefix;
        }

        public String pathPrefix() {
            return pathPrefix;
        }

        public void setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }
    }
}
