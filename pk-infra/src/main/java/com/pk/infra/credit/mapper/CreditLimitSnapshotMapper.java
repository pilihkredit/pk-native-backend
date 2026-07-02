package com.pk.infra.credit.mapper;

import com.pk.core.credit.port.CreditLimitSnapshotRepository.CreditLimitSnapshotData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreditLimitSnapshotMapper {
    int deleteByCreditApplicationId(@Param("creditApplicationId") long creditApplicationId);

    int insert(CreditLimitSnapshotData data);

    CreditLimitSnapshotData findByCreditApplicationId(@Param("creditApplicationId") long creditApplicationId);
}
