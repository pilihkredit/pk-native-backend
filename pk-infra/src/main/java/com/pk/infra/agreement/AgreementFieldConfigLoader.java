package com.pk.infra.agreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;

public class AgreementFieldConfigLoader {
    public static final String CONFIG_KEY = "agreementFieldConf";

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public AgreementFieldConfigLoader(
            AppConfigRepository appConfigRepository,
            ObjectMapper objectMapper
    ) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public AgreementFieldConfig load() {
        return appConfigRepository.findByKey(CONFIG_KEY)
                .map(this::parse)
                .orElseGet(AgreementFieldConfig::defaults);
    }

    private AgreementFieldConfig parse(AppConfigRepository.AppConfigRecord record) {
        try {
            AgreementFieldConfig config = objectMapper.readValue(
                    record.valueJson(),
                    AgreementFieldConfig.class
            );
            return config.withDefaults();
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    public record AgreementFieldConfig(
            String lenderName,
            String lenderLaw,
            String lenderAddress,
            String lenderRepName,
            String lenderRepTitle,
            String publisherName,
            String productType,
            String productDescription,
            String fundingPurpose,
            String provisionFeeDisplay
    ) {
        private static AgreementFieldConfig defaults() {
            return new AgreementFieldConfig(
                    "",
                    "",
                    "",
                    "",
                    "",
                    "PT Pendanaan Teknologi Nusa",
                    "Unsecured cash loan",
                    "",
                    "konsumtif multiguna",
                    "Rp 0"
            );
        }

        private AgreementFieldConfig withDefaults() {
            AgreementFieldConfig defaults = defaults();
            return new AgreementFieldConfig(
                    valueOrDefault(lenderName, defaults.lenderName),
                    valueOrDefault(lenderLaw, defaults.lenderLaw),
                    valueOrDefault(lenderAddress, defaults.lenderAddress),
                    valueOrDefault(lenderRepName, defaults.lenderRepName),
                    valueOrDefault(lenderRepTitle, defaults.lenderRepTitle),
                    valueOrDefault(publisherName, defaults.publisherName),
                    valueOrDefault(productType, defaults.productType),
                    valueOrDefault(productDescription, defaults.productDescription),
                    valueOrDefault(fundingPurpose, defaults.fundingPurpose),
                    valueOrDefault(provisionFeeDisplay, defaults.provisionFeeDisplay)
            );
        }

        private static String valueOrDefault(String value, String defaultValue) {
            return value == null ? defaultValue : value;
        }
    }
}
