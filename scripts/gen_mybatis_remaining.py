#!/usr/bin/env python3
from pathlib import Path

JAVA = Path("pk-infra/src/main/java/com/pk/infra")
RES = Path("pk-infra/src/main/resources/mapper")
I = "com.pk.infra.mybatis.typehandler.InstantTypeHandler"
B = "com.pk.infra.mybatis.typehandler.BooleanTinyintTypeHandler"

def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")

# ========== LoanApplication ==========
w(JAVA / "loan/mapper/LoanApplicationMapper.java", """
package com.pk.infra.loan.mapper;
import com.pk.core.loan.port.LoanApplicationRepository.LoanApplicationRecord;
import com.pk.infra.loan.repository.LoanApplicationInsertParam;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface LoanApplicationMapper {
    LoanApplicationRecord findById(@Param("id") long id);
    LoanApplicationRecord findByLoanApplyId(@Param("loanApplyId") String loanApplyId);
    LoanApplicationRecord findByLoanApplyIdAndProfileId(@Param("loanApplyId") String loanApplyId, @Param("profileId") long profileId);
    int insert(LoanApplicationInsertParam param);
    Long findIdByLoanApplyId(@Param("loanApplyId") String loanApplyId);
    int updateStatus(@Param("id") long id, @Param("status") String status, @Param("externalStatus") String externalStatus);
    int markSubmitted(@Param("id") long id, @Param("externalLoanApplyNo") String externalLoanApplyNo, @Param("externalStatus") String externalStatus);
    int updateDisbursementDetails(@Param("id") long id, @Param("billNo") String billNo, @Param("applyAmt") BigDecimal applyAmt,
        @Param("payAmount") BigDecimal payAmount, @Param("payTime") Instant payTime);
    int scheduleNextPoll(@Param("id") long id, @Param("nextPollAt") Instant nextPollAt);
    List<LoanApplicationRecord> findPendingPoll(@Param("limit") int limit);
}
""")

w(JAVA / "loan/repository/LoanApplicationInsertParam.java", """
package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanApplicationRepository.LoanApplicationInsert;
import java.math.BigDecimal;
public class LoanApplicationInsertParam {
    private String loanApplyId; private long creditApplicationId; private long quoteId;
    private long profileId; private long profileVersionId; private String status;
    private BigDecimal applyAmt; private String loanPurpose;
    public static LoanApplicationInsertParam from(LoanApplicationInsert insert) {
        LoanApplicationInsertParam p = new LoanApplicationInsertParam();
        p.loanApplyId = insert.loanApplyId(); p.creditApplicationId = insert.creditApplicationId();
        p.quoteId = insert.quoteId(); p.profileId = insert.profileId();
        p.profileVersionId = insert.profileVersionId(); p.status = insert.status();
        p.applyAmt = insert.applyAmt(); p.loanPurpose = insert.loanPurpose(); return p;
    }
    public String getLoanApplyId() { return loanApplyId; } public long getCreditApplicationId() { return creditApplicationId; }
    public long getQuoteId() { return quoteId; } public long getProfileId() { return profileId; }
    public long getProfileVersionId() { return profileVersionId; } public String getStatus() { return status; }
    public BigDecimal getApplyAmt() { return applyAmt; } public String getLoanPurpose() { return loanPurpose; }
}
""")

w(RES / "loan/LoanApplicationMapper.xml", f"""
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.pk.infra.loan.mapper.LoanApplicationMapper">
    <resultMap id="loanApplicationRecordMap" type="com.pk.core.loan.port.LoanApplicationRepository$LoanApplicationRecord">
        <constructor>
            <arg column="id" javaType="long"/><arg column="loan_apply_id" javaType="string"/>
            <arg column="credit_application_id" javaType="long"/><arg column="quote_id" javaType="long"/>
            <arg column="profile_id" javaType="long"/><arg column="profile_version_id" javaType="long"/>
            <arg column="external_loan_apply_no" javaType="string"/><arg column="bill_no" javaType="string"/>
            <arg column="status" javaType="string"/><arg column="external_status" javaType="string"/>
            <arg column="apply_amt" javaType="java.math.BigDecimal"/><arg column="pay_amount" javaType="java.math.BigDecimal"/>
            <arg column="pay_time" javaType="java.time.Instant" typeHandler="{I}"/>
        </constructor>
    </resultMap>
    <sql id="cols">id, loan_apply_id, credit_application_id, quote_id, profile_id, profile_version_id,
        external_loan_apply_no, bill_no, status, external_status, apply_amt, pay_amount, pay_time</sql>
    <select id="findById" resultMap="loanApplicationRecordMap">SELECT <include refid="cols"/> FROM loan_application WHERE id = #{{id}}</select>
    <select id="findByLoanApplyId" resultMap="loanApplicationRecordMap">SELECT <include refid="cols"/> FROM loan_application WHERE loan_apply_id = #{{loanApplyId}}</select>
    <select id="findByLoanApplyIdAndProfileId" resultMap="loanApplicationRecordMap">SELECT <include refid="cols"/> FROM loan_application WHERE loan_apply_id = #{{loanApplyId}} AND profile_id = #{{profileId}}</select>
    <insert id="insert">
        INSERT INTO loan_application (loan_apply_id, credit_application_id, quote_id, profile_id, profile_version_id, status, apply_amt, loan_purpose, submitted_at)
        VALUES (#{{loanApplyId}}, #{{creditApplicationId}}, #{{quoteId}}, #{{profileId}}, #{{profileVersionId}}, #{{status}}, #{{applyAmt}}, #{{loanPurpose}}, CURRENT_TIMESTAMP(3))
    </insert>
    <select id="findIdByLoanApplyId" resultType="long">SELECT id FROM loan_application WHERE loan_apply_id = #{{loanApplyId}}</select>
    <update id="updateStatus">
        UPDATE loan_application SET status = #{{status}}, external_status = #{{externalStatus}},
            finalized_at = CASE WHEN #{{status}} IN ('DISBURSED', 'REJECTED', 'FAILED', 'CANCEL') THEN CURRENT_TIMESTAMP(3) ELSE finalized_at END
        WHERE id = #{{id}}
    </update>
    <update id="markSubmitted">
        UPDATE loan_application SET status = 'PROCESSING', external_loan_apply_no = #{{externalLoanApplyNo}}, external_status = #{{externalStatus}},
            submitted_at = COALESCE(submitted_at, CURRENT_TIMESTAMP(3)) WHERE id = #{{id}}
    </update>
    <update id="updateDisbursementDetails">
        UPDATE loan_application SET bill_no = #{{billNo}}, apply_amt = COALESCE(#{{applyAmt}}, apply_amt),
            pay_amount = #{{payAmount}}, pay_time = #{{payTime, typeHandler={I}}} WHERE id = #{{id}}
    </update>
    <update id="scheduleNextPoll">UPDATE loan_application SET next_poll_at = #{{nextPollAt, typeHandler={I}}} WHERE id = #{{id}}</update>
    <select id="findPendingPoll" resultMap="loanApplicationRecordMap">
        SELECT <include refid="cols"/> FROM loan_application WHERE status = 'PROCESSING' AND next_poll_at IS NOT NULL
        AND next_poll_at &lt;= CURRENT_TIMESTAMP(3) ORDER BY next_poll_at LIMIT #{{limit}}
    </select>
</mapper>
""".replace("#{{", "#{").replace("}}", "}"))

w(JAVA / "loan/repository/LoanApplicationRepositoryImpl.java", """
package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.infra.loan.mapper.LoanApplicationMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository
public class LoanApplicationRepositoryImpl implements LoanApplicationRepository {
    private final LoanApplicationMapper mapper;
    public LoanApplicationRepositoryImpl(LoanApplicationMapper mapper) { this.mapper = mapper; }
    @Override public Optional<LoanApplicationRecord> findById(long id) { return Optional.ofNullable(mapper.findById(id)); }
    @Override public Optional<LoanApplicationRecord> findByLoanApplyId(String loanApplyId) { return Optional.ofNullable(mapper.findByLoanApplyId(loanApplyId)); }
    @Override public Optional<LoanApplicationRecord> findByLoanApplyIdAndProfileId(String loanApplyId, long profileId) {
        return Optional.ofNullable(mapper.findByLoanApplyIdAndProfileId(loanApplyId, profileId));
    }
    @Override public long insert(LoanApplicationInsert insert) {
        mapper.insert(LoanApplicationInsertParam.from(insert));
        Long id = mapper.findIdByLoanApplyId(insert.loanApplyId());
        if (id == null) throw new IllegalStateException("Failed to load inserted loan application");
        return id;
    }
    @Override public void updateStatus(long id, String status, String externalStatus) { mapper.updateStatus(id, status, externalStatus); }
    @Override public void markSubmitted(long id, String externalLoanApplyNo, String externalStatus) { mapper.markSubmitted(id, externalLoanApplyNo, externalStatus); }
    @Override public void updateDisbursementDetails(long id, String billNo, BigDecimal applyAmt, BigDecimal payAmount, Instant payTime) {
        mapper.updateDisbursementDetails(id, billNo, applyAmt, payAmount, payTime);
    }
    @Override public void scheduleNextPoll(long id, Instant nextPollAt) { mapper.scheduleNextPoll(id, nextPollAt); }
    @Override public List<LoanApplicationRecord> findPendingPoll(int limit) { return mapper.findPendingPoll(limit); }
}
""")

print("LoanApplication done")
