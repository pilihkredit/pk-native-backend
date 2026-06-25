package com.pk.infra.loan;

import com.pk.core.loan.port.ContractFileRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcContractFileRepository implements ContractFileRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcContractFileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsert(ContractFileUpsert upsert) {
        jdbcTemplate.update(
                """
                INSERT INTO contract_file (
                    loan_application_id,
                    bill_no,
                    contract_type,
                    contract_name,
                    contract_url,
                    fetched_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    bill_no = VALUES(bill_no),
                    contract_name = VALUES(contract_name),
                    contract_url = VALUES(contract_url),
                    fetched_at = VALUES(fetched_at)
                """,
                upsert.loanApplicationId(),
                upsert.billNo(),
                upsert.contractType(),
                upsert.contractName(),
                upsert.contractUrl(),
                Timestamp.from(upsert.fetchedAt())
        );
    }

    @Override
    public List<ContractFileRecord> findByLoanApplicationId(long loanApplicationId) {
        return jdbcTemplate.query(
                """
                SELECT id, loan_application_id, bill_no, contract_type, contract_name, contract_url, fetched_at
                FROM contract_file
                WHERE loan_application_id = ?
                ORDER BY contract_type
                """,
                (rs, rowNum) -> mapRecord(rs),
                loanApplicationId
        );
    }

    private static ContractFileRecord mapRecord(ResultSet rs) throws SQLException {
        Timestamp fetchedAt = rs.getTimestamp("fetched_at");
        return new ContractFileRecord(
                rs.getLong("id"),
                rs.getLong("loan_application_id"),
                rs.getString("bill_no"),
                rs.getString("contract_type"),
                rs.getString("contract_name"),
                rs.getString("contract_url"),
                fetchedAt == null ? null : fetchedAt.toInstant()
        );
    }
}
