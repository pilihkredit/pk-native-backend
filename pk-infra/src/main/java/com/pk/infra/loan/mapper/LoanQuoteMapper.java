package com.pk.infra.loan.mapper;

import com.pk.infra.loan.repository.LoanQuoteInsertParam;
import com.pk.infra.loan.repository.LoanQuoteRow;
import com.pk.infra.loan.repository.LoanQuoteTermInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanQuoteMapper {
    int insertQuote(LoanQuoteInsertParam param);

    int updateQuoteById(LoanQuoteInsertParam param);

    int deleteTermsByQuoteId(@Param("quoteId") long quoteId);

    int insertTerm(LoanQuoteTermInsertParam term);

    int countTermsByQuoteId(@Param("quoteId") long quoteId);

    LoanQuoteRow findByQuoteNo(@Param("quoteNo") String quoteNo);

    LoanQuoteRow findLatestByApplyProductRepay(
            @Param("applyId") String applyId,
            @Param("productCode") String productCode,
            @Param("repayMethod") String repayMethod
    );

    int countLoanApplicationReferences(
            @Param("quoteId") long quoteId,
            @Param("quoteNo") String quoteNo
    );
}
