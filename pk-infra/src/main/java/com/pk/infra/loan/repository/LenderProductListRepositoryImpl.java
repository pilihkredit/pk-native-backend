package com.pk.infra.loan.repository;

import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.port.LenderProductListRepository;
import com.pk.infra.loan.mapper.LenderProductListMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LenderProductListRepositoryImpl implements LenderProductListRepository {
    private final LenderProductListMapper mapper;

    public LenderProductListRepositoryImpl(LenderProductListMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductListTree> findById(long id) {
        return Optional.ofNullable(mapper.findHeaderById(id)).map(this::loadTree);
    }

    @Override
    public Optional<ProductListTree> findLatestByApplyId(String applyId) {
        return Optional.ofNullable(mapper.findLatestHeaderByApplyId(applyId)).map(this::loadTree);
    }

    @Override
    @Transactional
    public ProductListTree insertTree(ProductListInsert command) {
        LenderProductListInsertParam listParam = toListParam(command);
        mapper.insertList(listParam);
        if (listParam.getId() == null) {
            throw new IllegalStateException("Failed to insert lender product list header");
        }
        long listId = listParam.getId();
        insertProducts(listId, command.userId(), command.products());
        return findById(listId).orElseThrow(() -> new IllegalStateException("Failed to reload product list tree"));
    }

    private ProductListTree loadTree(LenderProductListHeaderRow headerRow) {
        ProductListHeader header = toHeader(headerRow);
        List<LenderProductRow> productRows = mapper.findProductsByListId(header.id());
        if (productRows == null || productRows.isEmpty()) {
            return new ProductListTree(header, List.of());
        }
        List<Long> productIds = productRows.stream().map(LenderProductRow::getId).toList();
        List<LenderProductRepayMethodRow> methodRows = mapper.findRepayMethodsByProductIds(productIds);
        Map<Long, List<LenderProductRepayMethodRow>> methodsByProduct = groupMethods(methodRows);
        List<Long> methodIds = methodRows == null
                ? List.of()
                : methodRows.stream().map(LenderProductRepayMethodRow::getId).toList();
        Map<Long, List<LenderProductUnevenRateRow>> ratesByMethod = methodIds.isEmpty()
                ? Map.of()
                : groupRates(mapper.findUnevenRatesByRepayMethodIds(methodIds));

        List<LenderLoanProduct> products = new ArrayList<>();
        for (LenderProductRow productRow : productRows) {
            List<LenderRepayMethod> repayMethods = new ArrayList<>();
            for (LenderProductRepayMethodRow methodRow : methodsByProduct.getOrDefault(productRow.getId(), List.of())) {
                List<LenderRepayMethod.UnevenBillRate> unevenRates = ratesByMethod
                        .getOrDefault(methodRow.getId(), List.of())
                        .stream()
                        .map(rate -> new LenderRepayMethod.UnevenBillRate(rate.getTermNum(), rate.getRepaymentRate()))
                        .toList();
                repayMethods.add(new LenderRepayMethod(
                        methodRow.getRepayMethod(),
                        methodRow.getCycleType(),
                        methodRow.getCycleInterval(),
                        methodRow.getCycleCount(),
                        methodRow.getTotalCycleInterval(),
                        methodRow.getRepayMethodType(),
                        null,
                        unevenRates
                ));
            }
            products.add(new LenderLoanProduct(
                    productRow.getProductCode(),
                    null,
                    productRow.getMinAmount(),
                    productRow.getMaxAmount(),
                    productRow.getComprehensiveRateUnit(),
                    productRow.getComprehensiveRate(),
                    List.copyOf(repayMethods)
            ));
        }
        return new ProductListTree(header, List.copyOf(products));
    }

    private void insertProducts(long listId, long userId, List<LenderLoanProduct> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        for (LenderLoanProduct product : products) {
            LenderProductInsertParam productParam = new LenderProductInsertParam();
            productParam.setProductListId(listId);
            productParam.setUserId(userId);
            productParam.setProductCode(product.productCode() == null ? "" : product.productCode());
            productParam.setMinAmount(product.minAmount());
            productParam.setMaxAmount(product.maxAmount());
            productParam.setComprehensiveRateUnit(product.comprehensiveRateUnit());
            productParam.setComprehensiveRate(product.comprehensiveRate());
            mapper.insertProduct(productParam);
            if (productParam.getId() == null) {
                throw new IllegalStateException("Failed to insert lender product");
            }
            insertRepayMethods(productParam.getId(), userId, product.repayMethods());
        }
    }

    private void insertRepayMethods(long productId, long userId, List<LenderRepayMethod> repayMethods) {
        if (repayMethods == null || repayMethods.isEmpty()) {
            return;
        }
        for (LenderRepayMethod repayMethod : repayMethods) {
            LenderProductRepayMethodInsertParam methodParam = new LenderProductRepayMethodInsertParam();
            methodParam.setProductId(productId);
            methodParam.setUserId(userId);
            methodParam.setRepayMethod(repayMethod.repayMethod() == null ? "" : repayMethod.repayMethod());
            methodParam.setCycleType(repayMethod.cycleType());
            methodParam.setCycleInterval(repayMethod.cycleInterval());
            methodParam.setCycleCount(repayMethod.cycleCount());
            methodParam.setTotalCycleInterval(repayMethod.totalCycleInterval());
            methodParam.setRepayMethodType(repayMethod.repayMethodType());
            mapper.insertRepayMethod(methodParam);
            if (methodParam.getId() == null) {
                throw new IllegalStateException("Failed to insert lender repay method");
            }
            insertUnevenRates(methodParam.getId(), userId, repayMethod.unevenBillsRepaymentRates());
        }
    }

    private void insertUnevenRates(
            long repayMethodId,
            long userId,
            List<LenderRepayMethod.UnevenBillRate> unevenRates
    ) {
        if (unevenRates == null || unevenRates.isEmpty()) {
            return;
        }
        for (LenderRepayMethod.UnevenBillRate unevenRate : unevenRates) {
            LenderProductUnevenRateInsertParam rateParam = new LenderProductUnevenRateInsertParam();
            rateParam.setRepayMethodId(repayMethodId);
            rateParam.setUserId(userId);
            rateParam.setTermNum(unevenRate.termNum());
            rateParam.setRepaymentRate(unevenRate.repaymentRate());
            mapper.insertUnevenRate(rateParam);
        }
    }

    private static LenderProductListInsertParam toListParam(ProductListInsert command) {
        LenderProductListInsertParam param = new LenderProductListInsertParam();
        param.setUserId(command.userId());
        param.setCreditApplicationId(command.creditApplicationId());
        
        param.setApplyId(command.applyId());
        param.setCreditApplyNo(command.creditApplyNo());
        param.setLenderUserId(command.lenderUserId());
        param.setCreditStatus(command.creditStatus());
        param.setProductStatus(command.productStatus());
        param.setContentHash(command.contentHash());
        param.setExternalInteractionId(command.externalInteractionId());
        param.setFetchedAt(command.fetchedAt());
        return param;
    }

    private static ProductListHeader toHeader(LenderProductListHeaderRow row) {
        return new ProductListHeader(
                row.getId(),
                row.getUserId(),
                row.getCreditApplicationId(),
                row.getApplyId(),
                row.getCreditApplyNo(),
                row.getLenderUserId(),
                row.getCreditStatus(),
                row.getProductStatus(),
                row.getContentHash(),
                row.getExternalInteractionId(),
                row.getFetchedAt()
        );
    }

    private static Map<Long, List<LenderProductRepayMethodRow>> groupMethods(
            List<LenderProductRepayMethodRow> methodRows
    ) {
        Map<Long, List<LenderProductRepayMethodRow>> grouped = new LinkedHashMap<>();
        if (methodRows == null) {
            return grouped;
        }
        for (LenderProductRepayMethodRow row : methodRows) {
            grouped.computeIfAbsent(row.getProductId(), ignored -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    private static Map<Long, List<LenderProductUnevenRateRow>> groupRates(
            List<LenderProductUnevenRateRow> rateRows
    ) {
        Map<Long, List<LenderProductUnevenRateRow>> grouped = new LinkedHashMap<>();
        if (rateRows == null) {
            return grouped;
        }
        for (LenderProductUnevenRateRow row : rateRows) {
            grouped.computeIfAbsent(row.getRepayMethodId(), ignored -> new ArrayList<>()).add(row);
        }
        return grouped;
    }
}
