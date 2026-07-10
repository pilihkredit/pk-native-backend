package com.pk.infra.loan.repository;

import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.infra.loan.LoanQuotePersistenceMapper;
import com.pk.infra.loan.mapper.LoanQuoteMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LoanQuoteRepositoryImpl implements LoanQuoteRepository {
    private final LoanQuoteMapper mapper;

    public LoanQuoteRepositoryImpl(LoanQuoteMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public LoanQuoteRecord insert(LoanQuoteInsert command, List<LoanQuoteTermInsert> terms) {
        LoanQuoteInsertParam param = new LoanQuoteInsertParam();
        LoanQuotePersistenceMapper.fillInsertParam(param, command);
        mapper.insertQuote(param);
        for (LoanQuoteTermInsert term : terms) {
            LoanQuoteTermInsertParam termParam = new LoanQuoteTermInsertParam();
            LoanQuotePersistenceMapper.fillTermInsertParam(param.getId(), termParam, term);
            mapper.insertTerm(termParam);
        }
        return LoanQuotePersistenceMapper.toRecord(param);
    }

    @Override
    public Optional<LoanQuoteRecord> findByQuoteNo(String quoteNo) {
        LoanQuoteRow row = mapper.findByQuoteNo(quoteNo);
        return row == null ? Optional.empty() : Optional.of(LoanQuotePersistenceMapper.toRecord(row));
    }

    @Override
    public int countTermsByQuoteId(long quoteId) {
        return mapper.countTermsByQuoteId(quoteId);
    }
}
