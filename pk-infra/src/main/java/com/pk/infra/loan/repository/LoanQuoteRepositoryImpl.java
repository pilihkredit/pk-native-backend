package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.infra.loan.mapper.LoanQuoteMapper;
import java.util.List; import java.util.Optional;
import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional;
@Repository public class LoanQuoteRepositoryImpl implements LoanQuoteRepository {
private final LoanQuoteMapper mapper; public LoanQuoteRepositoryImpl(LoanQuoteMapper mapper){this.mapper=mapper;}
@Override @Transactional public LoanQuoteRecord insert(LoanQuoteInsert command, List<LoanQuoteTermInsert> terms){
    LoanQuoteInsertParam p=LoanQuoteInsertParam.from(command); mapper.insertQuote(p);
    for(LoanQuoteTermInsert term:terms) mapper.insertTerm(LoanQuoteTermInsertParam.from(p.getId(),term));
    return new LoanQuoteRecord(p.getId(),command.quoteNo(),command.creditApplicationId(),command.mobileNo(),command.productSnapshotId(),
        command.productCode(),command.repayMethod(),command.applyAmt(),command.loanPrincipal(),command.payAmount(),
        command.schdAmount(),command.interest(),command.totalDays(),command.feeJson(),
        command.lastLenderRequestJson(),command.lastLenderResponseJson(),command.rawResponseJson(),command.quotedAt());}
@Override public int countTermsByQuoteId(long quoteId){return mapper.countTermsByQuoteId(quoteId);}
@Override public Optional<LoanQuoteRecord> findByQuoteNo(String quoteNo){return Optional.ofNullable(mapper.findByQuoteNo(quoteNo));}}
