package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import java.util.List;

public class PendanaanRepayCurrentOrderAdapter implements LenderRepayCurrentOrderPort {
    static final String REPAY_CURRENT_ORDER_PATH = "/api/open/v1/repay/current/order";
    static final String BUSINESS_TYPE = "REPAY_CURRENT_ORDER";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanRepayCurrentOrderAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void setCurrentOrder(LenderRepayCurrentOrderCommand command) {
        httpClient.post(
                REPAY_CURRENT_ORDER_PATH,
                buildRequestBody(command),
                BUSINESS_TYPE,
                command.partnerUserId()
        );
    }

    private String buildRequestBody(LenderRepayCurrentOrderCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            ArrayNode repayOrders = root.putArray("repayOrders");
            for (RepayOrderItem item : command.repayOrders()) {
                var orderNode = repayOrders.addObject();
                orderNode.put("loanApplyId", item.loanApplyId());
                ArrayNode termNos = orderNode.putArray("termNos");
                for (Integer termNo : item.termNos()) {
                    termNos.add(termNo);
                }
            }
            if (command.couponId() != null) {
                root.put("couponId", command.couponId());
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }
}
