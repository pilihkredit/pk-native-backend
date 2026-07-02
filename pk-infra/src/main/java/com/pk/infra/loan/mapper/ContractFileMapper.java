package com.pk.infra.loan.mapper;
import com.pk.core.loan.port.ContractFileRepository.ContractFileRecord;
import com.pk.core.loan.port.ContractFileRepository.ContractFileUpsert;
import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface ContractFileMapper {
    int upsert(ContractFileUpsert upsert);
    List<ContractFileRecord> findByLoanApplicationId(@Param("loanApplicationId") long loanApplicationId);
}
