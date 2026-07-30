package com.pk.infra.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AppConfigReviewSandboxConfigPortTest {
    @Test
    void resolvesEnabledUserScenarioFromAppConfig() {
        String value = """
                {
                  "enabled": true,
                  "users": [{"mobileNo": "628111000000", "scenarioCode": "APP_STORE"}],
                  "scenarios": {
                    "APP_STORE": {
                      "responses": {
                        "creditStatus": {"status": "SUCCESS", "fakeCreditLimit": 12000000},
                        "products": {"creditStatus": "SUCCESS", "products": []}
                      },
                      "minAmount": 500000,
                      "maxAmount": 3000000,
                      "amountStep": 100000,
                      "comprehensiveRate": 0.18,
                      "disbursementRate": 0.97,
                      "termCount": 6,
                      "termDays": 30,
                      "vaBankCode": "REVIEW_BANK",
                      "vaBankName": "Review Bank",
                      "vaNo": "0000000000000000",
                      "vas": [
                        {
                          "vaNo": "1111111111111",
                          "bankCode": "BANK_A",
                          "bankName": "Bank A",
                          "bankType": 1,
                          "icon": "https://example.invalid/bank-a.png",
                          "defaultFlag": true,
                          "disabled": false,
                          "show": true,
                          "bankChannels": [
                            {"bankChannel": "ATM", "instruction": "Review payment instructions", "defaultChannel": false}
                          ]
                        },
                        {
                          "vaNo": "2222222222222",
                          "bankCode": "BANK_B",
                          "bankName": "Bank B",
                          "bankType": 1,
                          "defaultFlag": false,
                          "disabled": false,
                          "show": true,
                          "bankChannels": []
                        }
                      ]
                    }
                  }
                }
                """;
        AppConfigRepository repository = key -> Optional.of(
                new AppConfigRepository.AppConfigRecord(1L, key, value)
        );
        var port = new AppConfigReviewSandboxConfigPort(repository, new ObjectMapper());

        var scenario = port.findEnabledScenario(" 628111000000 ").orElseThrow();

        assertThat(scenario.code()).isEqualTo("APP_STORE");
        assertThat(scenario.maxAmount()).isEqualByComparingTo(new BigDecimal("3000000"));
        assertThat(scenario.vas()).hasSize(2);
        assertThat(scenario.vas().getFirst().bankChannels()).hasSize(1);
        assertThat(scenario.vas().getFirst().defaultFlag()).isTrue();
        assertThat(scenario.response("creditStatus")).contains("12000000");
        assertThat(scenario.response("products")).contains("SUCCESS");
        assertThat(port.findEnabledScenario("628122000000")).isEmpty();
    }

    @Test
    void disablesAllReviewRoutingWhenTopLevelSwitchIsFalse() {
        String value = """
                {"enabled": false, "users": [{"mobileNo": "628111000000", "scenarioCode": "APP_STORE"}]}
                """;
        AppConfigRepository repository = key -> Optional.of(
                new AppConfigRepository.AppConfigRecord(1L, key, value)
        );
        var port = new AppConfigReviewSandboxConfigPort(repository, new ObjectMapper());

        assertThat(port.findEnabledScenario("628111000000")).isEmpty();
    }

    @Test
    void appliesWhitelistChangesWithoutAStaleRoutingWindow() {
        AtomicReference<String> value = new AtomicReference<>("""
                {"enabled": false, "users": []}
                """);
        AppConfigRepository repository = key -> Optional.of(
                new AppConfigRepository.AppConfigRecord(1L, key, value.get())
        );
        var port = new AppConfigReviewSandboxConfigPort(repository, new ObjectMapper());

        assertThat(port.findEnabledScenario("628111000000")).isEmpty();

        value.set("""
                {
                  "enabled": true,
                  "users": [{"mobileNo": "628111000000", "scenarioCode": "APP_STORE"}],
                  "scenarios": {
                    "APP_STORE": {
                      "minAmount": 500000,
                      "maxAmount": 3000000,
                      "amountStep": 100000,
                      "comprehensiveRate": 0.18,
                      "disbursementRate": 0.97,
                      "termCount": 6,
                      "termDays": 30,
                      "vaBankCode": "REVIEW_BANK",
                      "vaBankName": "Review Bank",
                      "vaNo": "0000000000000000"
                    }
                  }
                }
                """);

        assertThat(port.findEnabledScenario("628111000000")).isPresent();
    }
}
