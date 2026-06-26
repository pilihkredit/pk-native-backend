package com.pk.infra.loan;

import com.pk.core.loan.port.LoanQuoteRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcLoanQuoteRepository implements LoanQuoteRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLoanQuoteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public LoanQuoteRecord insert(LoanQuoteInsert command, List<LoanQuoteTermInsert> terms) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    """
                    INSERT INTO loan_quote (
                        quote_no,
                        credit_application_id,
                        product_snapshot_id,
                        product_code,
                        repay_method,
                        apply_amt,
                        loan_principal,
                        pay_amount,
                        schd_amount,
                        interest,
                        total_days,
                        fee_json,
                        raw_response_json,
                        quoted_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[] {"id"}
            );
            statement.setString(1, command.quoteNo());
            statement.setLong(2, command.creditApplicationId());
            if (command.productSnapshotId() == null) {
                statement.setObject(3, null);
            } else {
                statement.setLong(3, command.productSnapshotId());
            }
            statement.setString(4, command.productCode());
            statement.setString(5, command.repayMethod());
            statement.setBigDecimal(6, command.applyAmt());
            statement.setBigDecimal(7, command.loanPrincipal());
            statement.setBigDecimal(8, command.payAmount());
            statement.setBigDecimal(9, command.schdAmount());
            statement.setBigDecimal(10, command.interest());
            if (command.totalDays() == null) {
                statement.setObject(11, null);
            } else {
                statement.setInt(11, command.totalDays());
            }
            statement.setString(12, command.feeJson());
            statement.setString(13, command.rawResponseJson());
            statement.setTimestamp(14, Timestamp.from(command.quotedAt()));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert loan quote");
        }
        long quoteId = key.longValue();
        for (LoanQuoteTermInsert term : terms) {
            jdbcTemplate.update(
                    """
                    INSERT INTO loan_quote_term (
                        quote_id,
                        term_no,
                        due_date,
                        schd_amount,
                        schd_principal,
                        schd_interest,
                        fee_json
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    quoteId,
                    term.termNo(),
                    term.dueDate() == null ? null : Timestamp.from(term.dueDate()),
                    term.schdAmount(),
                    term.schdPrincipal(),
                    term.schdInterest(),
                    term.feeJson()
            );
        }
        return new LoanQuoteRecord(
                quoteId,
                command.quoteNo(),
                command.creditApplicationId(),
                command.productSnapshotId(),
                command.productCode(),
                command.repayMethod(),
                command.applyAmt(),
                command.loanPrincipal(),
                command.payAmount(),
                command.schdAmount(),
                command.interest(),
                command.totalDays(),
                command.feeJson(),
                command.rawResponseJson(),
                command.quotedAt()
        );
    }

    @Override
    public int countTermsByQuoteId(long quoteId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM loan_quote_term WHERE quote_id = ?",
                Integer.class,
                quoteId
        );
        return count == null ? 0 : count;
    }

    @Override
    public Optional<LoanQuoteRecord> findByQuoteNo(String quoteNo) {
        return jdbcTemplate.query(
                """
                SELECT id, quote_no, credit_application_id, product_snapshot_id, product_code, repay_method,
                       apply_amt, loan_principal, pay_amount, schd_amount, interest, total_days,
                       fee_json, raw_response_json, quoted_at
                FROM loan_quote
                WHERE quote_no = ?
                """,
                rs -> rs.next()
                        ? Optional.of(mapRecord(rs))
                        : Optional.empty(),
                quoteNo
        );
    }

    private static LoanQuoteRecord mapRecord(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp quotedAt = rs.getTimestamp("quoted_at");
        long productSnapshotId = rs.getLong("product_snapshot_id");
        boolean productSnapshotWasNull = rs.wasNull();
        int totalDays = rs.getInt("total_days");
        boolean totalDaysWasNull = rs.wasNull();
        return new LoanQuoteRecord(
                rs.getLong("id"),
                rs.getString("quote_no"),
                rs.getLong("credit_application_id"),
                productSnapshotWasNull ? null : productSnapshotId,
                rs.getString("product_code"),
                rs.getString("repay_method"),
                rs.getBigDecimal("apply_amt"),
                rs.getBigDecimal("loan_principal"),
                rs.getBigDecimal("pay_amount"),
                rs.getBigDecimal("schd_amount"),
                rs.getBigDecimal("interest"),
                totalDaysWasNull ? null : totalDays,
                rs.getString("fee_json"),
                rs.getString("raw_response_json"),
                quotedAt == null ? Instant.EPOCH : quotedAt.toInstant()
        );
    }
}
