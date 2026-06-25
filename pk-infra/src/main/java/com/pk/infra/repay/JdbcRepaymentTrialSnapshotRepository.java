package com.pk.infra.repay;

import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcRepaymentTrialSnapshotRepository implements RepaymentTrialSnapshotRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcRepaymentTrialSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public TrialSnapshotRecord insert(TrialSnapshotInsert insert, List<TrialOrderInsert> orders) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    """
                    INSERT INTO repayment_trial_snapshot (
                        trial_no, profile_id, trial_type, total_bill_count, total_should_amount,
                        total_reduction_amount, default_va_json, raw_response_json
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[] {"id"}
            );
            statement.setString(1, insert.trialNo());
            statement.setLong(2, insert.profileId());
            statement.setString(3, insert.trialType());
            if (insert.totalBillCount() == null) {
                statement.setObject(4, null);
            } else {
                statement.setInt(4, insert.totalBillCount());
            }
            statement.setBigDecimal(5, insert.totalShouldAmount());
            statement.setBigDecimal(6, insert.totalReductionAmount());
            statement.setString(7, insert.defaultVaJson());
            statement.setString(8, insert.rawResponseJson());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert repayment trial snapshot");
        }
        long trialId = key.longValue();
        for (TrialOrderInsert order : orders) {
            jdbcTemplate.update(
                    """
                    INSERT INTO repayment_trial_order (
                        trial_id, loan_application_id, loan_apply_id, settle, term_nos_json,
                        should_amount, bill_status, term_info_json
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    trialId,
                    order.loanApplicationId(),
                    order.loanApplyId(),
                    order.settle() ? 1 : 0,
                    order.termNosJson(),
                    order.shouldAmount(),
                    order.billStatus(),
                    order.termInfoJson()
            );
        }
        Instant createdAt = Instant.now();
        return new TrialSnapshotRecord(
                trialId,
                insert.trialNo(),
                insert.profileId(),
                insert.trialType(),
                insert.totalBillCount(),
                insert.totalShouldAmount(),
                insert.totalReductionAmount(),
                insert.defaultVaJson(),
                insert.rawResponseJson(),
                createdAt
        );
    }

    @Override
    public Optional<TrialSnapshotRecord> findByTrialNo(String trialNo) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT id, trial_no, profile_id, trial_type, total_bill_count, total_should_amount,
                           total_reduction_amount, default_va_json, raw_response_json, created_at
                    FROM repayment_trial_snapshot
                    WHERE trial_no = ?
                    """,
                    (rs, rowNum) -> {
                        int totalBillCount = rs.getInt("total_bill_count");
                        Integer totalBillCountValue = rs.wasNull() ? null : totalBillCount;
                        return new TrialSnapshotRecord(
                                rs.getLong("id"),
                                rs.getString("trial_no"),
                                rs.getLong("profile_id"),
                                rs.getString("trial_type"),
                                totalBillCountValue,
                                rs.getBigDecimal("total_should_amount"),
                                rs.getBigDecimal("total_reduction_amount"),
                                rs.getString("default_va_json"),
                                rs.getString("raw_response_json"),
                                rs.getTimestamp("created_at").toInstant()
                        );
                    },
                    trialNo
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
