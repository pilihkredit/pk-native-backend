package com.pk.infra.loan.repository;
import com.pk.core.loan.port.ContractFileRepository;
import com.pk.infra.loan.mapper.ContractFileMapper;
import java.util.List; import org.springframework.stereotype.Repository;
@Repository public class ContractFileRepositoryImpl implements ContractFileRepository {
private final ContractFileMapper mapper; public ContractFileRepositoryImpl(ContractFileMapper mapper){this.mapper=mapper;}
@Override public void upsert(ContractFileUpsert upsert){mapper.upsert(upsert);}
@Override public List<ContractFileRecord> findByLoanApplicationId(long loanApplicationId){return mapper.findByLoanApplicationId(loanApplicationId);}}
