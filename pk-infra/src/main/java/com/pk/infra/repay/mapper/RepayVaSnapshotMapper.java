package com.pk.infra.repay.mapper;
import com.pk.core.repay.port.RepayVaSnapshotRepository.VaSnapshotRecord;
import com.pk.infra.repay.repository.RepayVaSnapshotInsertParam;
import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface RepayVaSnapshotMapper {
    int insertSnapshot(RepayVaSnapshotInsertParam param);
    List<VaSnapshotRecord> findLatestByProfileId(@Param("profileId") long profileId);
}
