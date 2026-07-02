package com.pk.infra.reference.mapper;
import com.pk.core.reference.BankReference;
import com.pk.infra.reference.repository.RefBankUpsertParam;
import java.time.Instant; import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface RefBankMapper {
    List<BankReference> findAllActive(@Param("status") String status);
    Instant findLatestSyncedAt(@Param("status") String status);
    int countActiveByBankCode(@Param("status") String status, @Param("bankCode") String bankCode);
    int deactivateAll(@Param("status") String status);
    int upsertBank(RefBankUpsertParam param);
}
