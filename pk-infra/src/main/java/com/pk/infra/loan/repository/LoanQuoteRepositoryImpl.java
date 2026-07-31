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
    public LoanQuoteRecord upsert(LoanQuoteInsert command, List<LoanQuoteTermInsert> terms) {
        if (command == null || command.quote() == null) {
            throw new IllegalArgumentException("Loan quote insert requires quote detail");
        }
        LoanQuoteInsertParam param = new LoanQuoteInsertParam();
        LoanQuotePersistenceMapper.fillInsertParam(param, command);

        String applyId = command.quote().applyId();
        String productCode = command.quote().productCode();
        String repayMethod = command.quote().repayMethod();
        LoanQuoteRow latest = mapper.findLatestByApplyProductRepay(applyId, productCode, repayMethod);
        boolean overwriteDraft = latest != null && !isReferenced(latest);

        if (overwriteDraft) {
            param.setId(latest.getId());
            int updated = mapper.updateQuoteById(param);
            if (updated != 1) {
                throw new IllegalStateException("Failed to update draft loan_quote id=" + latest.getId());
            }
        } else {
            mapper.insertQuote(param);
            if (param.getId() <= 0) {
                throw new IllegalStateException("Failed to insert loan_quote");
            }
        }

        long quoteId = param.getId();
        mapper.deleteTermsByQuoteId(quoteId);
        if (terms != null) {
            for (LoanQuoteTermInsert term : terms) {
                LoanQuoteTermInsertParam termParam = new LoanQuoteTermInsertParam();
                LoanQuotePersistenceMapper.fillTermInsertParam(quoteId, termParam, term);
                mapper.insertTerm(termParam);
            }
        }
        return LoanQuotePersistenceMapper.toRecord(param);
    }

    private boolean isReferenced(LoanQuoteRow quote) {
        return mapper.countLoanApplicationReferences(quote.getId(), quote.getQuoteNo()) > 0;
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
