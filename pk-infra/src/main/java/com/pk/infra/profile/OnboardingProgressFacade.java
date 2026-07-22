package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.onboarding.OnboardingModuleCode;
import com.pk.core.profile.port.LenderProfileQueryPort;
import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding progress based on lender {@code POST /api/open/v1/user/info/query}.
 * A module counts as completed only when the lender returns a non-null payload for it.
 * When the lender reports user/application not found ({@code A000010}/{@code L000010}),
 * progress is returned as incomplete with every module missing (success path for clients).
 */
public class OnboardingProgressFacade {
    public static final String KYC_INCOMPLETE = "INCOMPLETE";
    public static final String KYC_SYNCED = "SYNCED";

    private static final List<String> LENDER_QUERY_MODULES = List.of(
            "profile",
            "contact",
            "identity",
            "bankCard",
            "device"
    );

    private static final List<ModuleMapping> MODULE_MAPPINGS = List.of(
            new ModuleMapping(OnboardingModuleCode.PERSONAL, "profile"),
            new ModuleMapping(OnboardingModuleCode.BANK_CARD, "bankCard"),
            new ModuleMapping(OnboardingModuleCode.CONTACT, "contact"),
            new ModuleMapping(OnboardingModuleCode.DEVICE, "device"),
            new ModuleMapping(OnboardingModuleCode.IDENTITY, "identity")
    );

    private final LenderProfileQueryPort lenderProfileQueryPort;
    private final ObjectMapper objectMapper;

    public OnboardingProgressFacade(
            LenderProfileQueryPort lenderProfileQueryPort,
            ObjectMapper objectMapper
    ) {
        this.lenderProfileQueryPort = lenderProfileQueryPort;
        this.objectMapper = objectMapper;
    }

    public OnboardingProgressResult getProgress(long profileId, String partnerUserId) {
        JsonNode lenderData = queryLender(partnerUserId);

        List<String> completedModules = new ArrayList<>();
        List<String> missingModules = new ArrayList<>();
        for (ModuleMapping mapping : MODULE_MAPPINGS) {
            if (hasModuleData(lenderData, mapping.lenderModuleKey())) {
                completedModules.add(mapping.onboardingModuleCode());
            } else {
                missingModules.add(mapping.onboardingModuleCode());
            }
        }

        String kycStatus = missingModules.isEmpty() ? KYC_SYNCED : KYC_INCOMPLETE;
        return new OnboardingProgressResult(
                partnerUserId,
                kycStatus,
                List.copyOf(completedModules),
                List.copyOf(missingModules)
        );
    }

    private JsonNode queryLender(String partnerUserId) {
        if (partnerUserId == null || partnerUserId.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "partnerUserId is required");
        }
        try {
            LenderProfileQueryPort.LenderProfileQueryResult result = lenderProfileQueryPort.query(
                    new LenderProfileQueryPort.LenderProfileQueryCommand(
                            partnerUserId,
                            LENDER_QUERY_MODULES
                    )
            );
            return parseResponse(result.rawResponseJson());
        } catch (ApiException exception) {
            // Lender has no user yet (A000010 / L000010): treat as all modules incomplete.
            if (exception.apiCode() == ApiCode.UPSTREAM_APPLICATION_NOT_FOUND) {
                return objectMapper.createObjectNode();
            }
            throw exception;
        }
    }

    private JsonNode parseResponse(String rawResponseJson) {
        if (rawResponseJson == null || rawResponseJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(rawResponseJson);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    static boolean hasModuleData(JsonNode root, String lenderModuleKey) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return false;
        }
        JsonNode node = root.get(lenderModuleKey);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return false;
        }
        if (node.isObject() && node.isEmpty()) {
            return false;
        }
        if (node.isArray() && node.isEmpty()) {
            return false;
        }
        return true;
    }

    private record ModuleMapping(String onboardingModuleCode, String lenderModuleKey) {
    }

    public record OnboardingProgressResult(
            String partnerUserId,
            String kycStatus,
            List<String> completedModules,
            List<String> missingModules
    ) {
    }
}
