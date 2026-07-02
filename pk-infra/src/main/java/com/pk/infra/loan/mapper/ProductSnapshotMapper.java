package com.pk.infra.loan.mapper;
import com.pk.infra.loan.repository.ProductSnapshotInsertParam;
import com.pk.core.loan.port.ProductSnapshotRepository.ProductSnapshotRecord;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface ProductSnapshotMapper {
    int insert(ProductSnapshotInsertParam param);
    ProductSnapshotRecord findBySnapshotNo(@Param("snapshotNo") String snapshotNo);
    ProductSnapshotRecord findLatestByCreditApplicationId(@Param("creditApplicationId") long creditApplicationId);
}
