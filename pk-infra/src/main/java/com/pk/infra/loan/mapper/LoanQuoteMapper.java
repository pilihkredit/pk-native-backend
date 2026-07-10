package com.pk.infra.loan.mapper;
import com.pk.infra.loan.repository.LoanQuoteInsertParam;
import com.pk.infra.loan.repository.LoanQuoteRow;
import com.pk.infra.loan.repository.LoanQuoteTermInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface LoanQuoteMapper {
    int insertQuote(LoanQuoteInsertParam param);
    int insertTerm(LoanQuoteTermInsertParam term);
    int countTermsByQuoteId(@Param("quoteId") long quoteId);
    LoanQuoteRow findByQuoteNo(@Param("quoteNo") String quoteNo);
}
