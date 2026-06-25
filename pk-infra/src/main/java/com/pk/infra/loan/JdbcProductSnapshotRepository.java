package com.pk.infra.loan;

import com.pk.core.loan.port.ProductSnapshotRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProductSnapshotRepository implements ProductSnapshotRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProductSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ProductSnapshotRecord insert(ProductSnapshotInsert command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    """
                    INSERT INTO pk_product_snapshot (
                        snapshot_no,
                        apply_id,
                        credit_application_id,
                        credit_status,
                        product_status,
                        products_json,
                        fetched_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[] {"id"}
            );
            statement.setString(1, command.snapshotNo());
            statement.setString(2, command.applyId());
            statement.setLong(3, command.creditApplicationId());
            statement.setString(4, command.creditStatus());
            statement.setString(5, command.productStatus());
            statement.setString(6, command.productsJson());
            statement.setTimestamp(7, Timestamp.from(command.fetchedAt()));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert product snapshot");
        }
        return new ProductSnapshotRecord(
                key.longValue(),
                command.snapshotNo(),
                command.applyId(),
                command.creditApplicationId(),
                command.creditStatus(),
                command.productStatus(),
                command.productsJson(),
                command.fetchedAt()
        );
    }

    @Override
    public Optional<ProductSnapshotRecord> findBySnapshotNo(String snapshotNo) {
        return jdbcTemplate.query(
                """
                SELECT id, snapshot_no, apply_id, credit_application_id, credit_status, product_status,
                       products_json, fetched_at
                FROM pk_product_snapshot
                WHERE snapshot_no = ?
                """,
                rs -> rs.next()
                        ? Optional.of(mapRecord(rs))
                        : Optional.empty(),
                snapshotNo
        );
    }

    @Override
    public Optional<ProductSnapshotRecord> findLatestByCreditApplicationId(long creditApplicationId) {
        return jdbcTemplate.query(
                """
                SELECT id, snapshot_no, apply_id, credit_application_id, credit_status, product_status,
                       products_json, fetched_at
                FROM pk_product_snapshot
                WHERE credit_application_id = ?
                ORDER BY fetched_at DESC, id DESC
                LIMIT 1
                """,
                rs -> rs.next()
                        ? Optional.of(mapRecord(rs))
                        : Optional.empty(),
                creditApplicationId
        );
    }

    private static ProductSnapshotRecord mapRecord(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp fetchedAt = rs.getTimestamp("fetched_at");
        return new ProductSnapshotRecord(
                rs.getLong("id"),
                rs.getString("snapshot_no"),
                rs.getString("apply_id"),
                rs.getLong("credit_application_id"),
                rs.getString("credit_status"),
                rs.getString("product_status"),
                rs.getString("products_json"),
                fetchedAt == null ? Instant.EPOCH : fetchedAt.toInstant()
        );
    }
}
