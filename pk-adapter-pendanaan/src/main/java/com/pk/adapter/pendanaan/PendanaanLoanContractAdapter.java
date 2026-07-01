package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.loan.port.LenderLoanContractPort;
import java.util.ArrayList;
import java.util.List;

public class PendanaanLoanContractAdapter implements LenderLoanContractPort {
    static final String CONTRACT_LIST_PATH = PendanaanOpenApiPaths.LOAN_CONTRACT_LIST;
    static final String BUSINESS_TYPE = "LOAN_CONTRACT_LIST";

    private final PendanaanHttpClient httpClient;

    public PendanaanLoanContractAdapter(PendanaanHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public LenderLoanContractListResult listContracts(String loanApplyId) {
        String requestBody = PendanaanLoanRequestMapper.buildContractListBody(loanApplyId);
        JsonNode data = httpClient.post(CONTRACT_LIST_PATH, requestBody, BUSINESS_TYPE, loanApplyId);
        if (data == null || data.isNull()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new LenderLoanContractListResult(
                requiredText(data, "loanApplyId"),
                textOrNull(data.get("loanApplyNo")),
                textOrNull(data.get("billNo")),
                mapContracts(data.get("contracts"))
        );
    }

    private static List<LenderLoanContract> mapContracts(JsonNode contractsNode) {
        if (contractsNode == null || !contractsNode.isArray()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderLoanContract> contracts = new ArrayList<>();
        for (JsonNode contractNode : contractsNode) {
            contracts.add(new LenderLoanContract(
                    requiredText(contractNode, "contractType"),
                    requiredText(contractNode, "contractName"),
                    requiredText(contractNode, "contractUrl")
            ));
        }
        return List.copyOf(contracts);
    }

    private static String requiredText(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        String value = valueNode.asText();
        if (value.isBlank()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return value;
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }
}
