package com.pk.infra.loan.mapper;

import com.pk.core.loan.port.LenderProductLatestRepository;
import com.pk.infra.loan.repository.LenderProductLatestInsertParam;
import com.pk.infra.loan.repository.LenderProductRepayMethodLatestInsertParam;
import com.pk.infra.loan.repository.LenderProductUnevenRateLatestInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LenderProductLatestMapper {
    Long findIdByCreditApplicationId(@Param("creditApplicationId") long creditApplicationId);

    void upsertListLatest(LenderProductLatestRepository.ReplaceLatestCommand command);

    int deleteUnevenRatesByListLatestId(@Param("listLatestId") long listLatestId);

    int deleteRepayMethodsByListLatestId(@Param("listLatestId") long listLatestId);

    int deleteProductsByListLatestId(@Param("listLatestId") long listLatestId);

    int insertProduct(LenderProductLatestInsertParam param);

    int insertRepayMethod(LenderProductRepayMethodLatestInsertParam param);

    int insertUnevenRate(LenderProductUnevenRateLatestInsertParam param);
}
