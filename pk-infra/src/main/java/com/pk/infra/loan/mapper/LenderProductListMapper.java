package com.pk.infra.loan.mapper;

import com.pk.infra.loan.repository.LenderProductInsertParam;
import com.pk.infra.loan.repository.LenderProductListHeaderRow;
import com.pk.infra.loan.repository.LenderProductListInsertParam;
import com.pk.infra.loan.repository.LenderProductRepayMethodInsertParam;
import com.pk.infra.loan.repository.LenderProductRepayMethodRow;
import com.pk.infra.loan.repository.LenderProductRow;
import com.pk.infra.loan.repository.LenderProductUnevenRateInsertParam;
import com.pk.infra.loan.repository.LenderProductUnevenRateRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LenderProductListMapper {
    int insertList(LenderProductListInsertParam param);

    int insertProduct(LenderProductInsertParam param);

    int insertRepayMethod(LenderProductRepayMethodInsertParam param);

    int insertUnevenRate(LenderProductUnevenRateInsertParam param);

    LenderProductListHeaderRow findHeaderById(@Param("id") long id);

    LenderProductListHeaderRow findLatestHeaderByApplyId(@Param("applyId") String applyId);

    List<LenderProductRow> findProductsByListId(@Param("productListId") long productListId);

    List<LenderProductRepayMethodRow> findRepayMethodsByProductIds(@Param("productIds") List<Long> productIds);

    List<LenderProductUnevenRateRow> findUnevenRatesByRepayMethodIds(
            @Param("repayMethodIds") List<Long> repayMethodIds
    );
}
