package com.pk.infra.reference;

import com.pk.core.reference.BankReference;
import com.pk.core.reference.port.RefBankRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRefBankRepository implements RefBankRepository {
    static final String STATUS_ACTIVE = "ACTIVE";

    private final JdbcTemplate jdbcTemplate;

    public JdbcRefBankRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<BankReference> findAllActive() {
        return jdbcTemplate.query(
                """
                SELECT bank_code, bank_name, bank_type, icon_url
                FROM ref_bank
                WHERE status = ?
                ORDER BY bank_name
                """,
                (rs, rowNum) -> new BankReference(
                        rs.getString("bank_code"),
                        rs.getString("bank_name"),
                        (Integer) rs.getObject("bank_type"),
                        rs.getString("icon_url")
                ),
                STATUS_ACTIVE
        );
    }

    @Override
    public Optional<Instant> findLatestSyncedAt() {
        try {
            Timestamp syncedAt = jdbcTemplate.queryForObject(
                    "SELECT MAX(synced_at) FROM ref_bank WHERE status = ?",
                    Timestamp.class,
                    STATUS_ACTIVE
            );
            return syncedAt == null ? Optional.empty() : Optional.of(syncedAt.toInstant());
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsActiveBankCode(String bankCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ref_bank WHERE status = ? AND bank_code = ?",
                Integer.class,
                STATUS_ACTIVE,
                bankCode
        );
        return count != null && count > 0;
    }

    @Override
    public void replaceAll(List<BankReference> banks, Instant syncedAt) {
        jdbcTemplate.update("UPDATE ref_bank SET status = 'INACTIVE' WHERE status = ?", STATUS_ACTIVE);
        Timestamp syncedTimestamp = Timestamp.from(syncedAt);
        for (BankReference bank : banks) {
            jdbcTemplate.update(
                    """
                    INSERT INTO ref_bank (
                        bank_code,
                        bank_name,
                        bank_type,
                        icon_url,
                        status,
                        synced_at
                    ) VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        bank_name = VALUES(bank_name),
                        bank_type = VALUES(bank_type),
                        icon_url = VALUES(icon_url),
                        status = VALUES(status),
                        synced_at = VALUES(synced_at)
                    """,
                    bank.bankCode(),
                    bank.bankName(),
                    bank.bankType(),
                    bank.iconUrl(),
                    STATUS_ACTIVE,
                    syncedTimestamp
            );
        }
    }
}
