package com.pk.infra.loan.repository;

import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.port.LenderProductLatestRepository;
import com.pk.infra.loan.mapper.LenderProductLatestMapper;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LenderProductLatestRepositoryImpl implements LenderProductLatestRepository {
    private final LenderProductLatestMapper mapper;

    public LenderProductLatestRepositoryImpl(LenderProductLatestMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void replaceLatest(ReplaceLatestCommand command) {
        Long existingId = mapper.findIdByCreditApplicationId(command.creditApplicationId());
        if (existingId != null) {
            mapper.deleteUnevenRatesByListLatestId(existingId);
            mapper.deleteRepayMethodsByListLatestId(existingId);
            mapper.deleteProductsByListLatestId(existingId);
        }

        mapper.upsertListLatest(command);
        long listLatestId = requireListLatestId(command.creditApplicationId());
        insertProducts(listLatestId, command.products());
    }

    private long requireListLatestId(long creditApplicationId) {
        Long listLatestId = mapper.findIdByCreditApplicationId(creditApplicationId);
        if (listLatestId == null) {
            throw new IllegalStateException("Failed to load lender product list latest id");
        }
        return listLatestId;
    }

    private void insertProducts(long listLatestId, List<LenderLoanProduct> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        for (int productIndex = 0; productIndex < products.size(); productIndex++) {
            LenderLoanProduct product = products.get(productIndex);
            LenderProductLatestInsertParam productParam = new LenderProductLatestInsertParam();
            productParam.setProductListLatestId(listLatestId);
            productParam.setProductCode(product.productCode() == null ? "" : product.productCode());
            productParam.setProductName(product.productName() == null ? "" : product.productName());
            productParam.setMinAmount(product.minAmount());
            productParam.setMaxAmount(product.maxAmount());
            productParam.setComprehensiveRateUnit(product.comprehensiveRateUnit());
            productParam.setComprehensiveRate(product.comprehensiveRate());
            productParam.setSortOrder(productIndex);
            mapper.insertProduct(productParam);
            insertRepayMethods(productParam.getId(), product.repayMethods());
        }
    }

    private void insertRepayMethods(long productLatestId, List<LenderRepayMethod> repayMethods) {
        if (repayMethods == null || repayMethods.isEmpty()) {
            return;
        }
        for (int methodIndex = 0; methodIndex < repayMethods.size(); methodIndex++) {
            LenderRepayMethod repayMethod = repayMethods.get(methodIndex);
            LenderProductRepayMethodLatestInsertParam methodParam = new LenderProductRepayMethodLatestInsertParam();
            methodParam.setProductLatestId(productLatestId);
            methodParam.setRepayMethod(repayMethod.repayMethod() == null ? "" : repayMethod.repayMethod());
            methodParam.setCycleType(repayMethod.cycleType());
            methodParam.setCycleInterval(repayMethod.cycleInterval());
            methodParam.setCycleCount(repayMethod.cycleCount());
            methodParam.setTotalCycleInterval(repayMethod.totalCycleInterval());
            methodParam.setRepayMethodType(repayMethod.repayMethodType());
            methodParam.setUnevenBillsRepaymentRateJson(repayMethod.unevenBillsRepaymentRateRaw());
            methodParam.setSortOrder(methodIndex);
            mapper.insertRepayMethod(methodParam);
            insertUnevenRates(methodParam.getId(), repayMethod.unevenBillsRepaymentRates());
        }
    }

    private void insertUnevenRates(long repayMethodLatestId, List<LenderRepayMethod.UnevenBillRate> unevenRates) {
        if (unevenRates == null || unevenRates.isEmpty()) {
            return;
        }
        for (int rateIndex = 0; rateIndex < unevenRates.size(); rateIndex++) {
            LenderRepayMethod.UnevenBillRate unevenRate = unevenRates.get(rateIndex);
            LenderProductUnevenRateLatestInsertParam rateParam = new LenderProductUnevenRateLatestInsertParam();
            rateParam.setRepayMethodLatestId(repayMethodLatestId);
            rateParam.setTermNum(unevenRate.termNum());
            rateParam.setRepaymentRate(unevenRate.repaymentRate());
            rateParam.setSortOrder(rateIndex);
            mapper.insertUnevenRate(rateParam);
        }
    }
}
