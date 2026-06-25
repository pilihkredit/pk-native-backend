package com.pk.infra.reference;

import com.pk.core.reference.AreaReference;
import com.pk.core.reference.port.RefAreaRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRefAreaRepository implements RefAreaRepository {
    static final String STATUS_ACTIVE = "ACTIVE";

    private final JdbcTemplate jdbcTemplate;

    public JdbcRefAreaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AreaReference> findActiveByParentCode(String parentCode) {
        String normalizedParent = AreaReferenceSupport.normalizeParentCode(parentCode);
        return jdbcTemplate.query(
                """
                SELECT area_code, area_name, parent_code, area_level
                FROM ref_area
                WHERE status = ? AND COALESCE(parent_code, '') = ?
                ORDER BY area_name
                """,
                (rs, rowNum) -> new AreaReference(
                        rs.getString("area_code"),
                        rs.getString("area_name"),
                        AreaReferenceSupport.normalizeParentCode(rs.getString("parent_code")),
                        rs.getInt("area_level")
                ),
                STATUS_ACTIVE,
                normalizedParent
        );
    }

    @Override
    public Optional<Instant> findLatestSyncedAtByParent(String parentCode) {
        String normalizedParent = AreaReferenceSupport.normalizeParentCode(parentCode);
        try {
            Timestamp syncedAt = jdbcTemplate.queryForObject(
                    """
                    SELECT MAX(synced_at)
                    FROM ref_area
                    WHERE status = ? AND COALESCE(parent_code, '') = ?
                    """,
                    Timestamp.class,
                    STATUS_ACTIVE,
                    normalizedParent
            );
            return syncedAt == null ? Optional.empty() : Optional.of(syncedAt.toInstant());
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AreaReference> findActiveByCode(String areaCode) {
        List<AreaReference> rows = jdbcTemplate.query(
                """
                SELECT area_code, area_name, parent_code, area_level
                FROM ref_area
                WHERE status = ? AND area_code = ?
                LIMIT 1
                """,
                (rs, rowNum) -> new AreaReference(
                        rs.getString("area_code"),
                        rs.getString("area_name"),
                        AreaReferenceSupport.normalizeParentCode(rs.getString("parent_code")),
                        rs.getInt("area_level")
                ),
                STATUS_ACTIVE,
                areaCode
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean existsActiveAreaCode(String areaCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ref_area WHERE status = ? AND area_code = ?",
                Integer.class,
                STATUS_ACTIVE,
                areaCode
        );
        return count != null && count > 0;
    }

    @Override
    public void replaceChildrenByParent(String parentCode, List<AreaReference> areas, Instant syncedAt) {
        String normalizedParent = AreaReferenceSupport.normalizeParentCode(parentCode);
        jdbcTemplate.update(
                "UPDATE ref_area SET status = 'INACTIVE' WHERE status = ? AND COALESCE(parent_code, '') = ?",
                STATUS_ACTIVE,
                normalizedParent
        );
        Timestamp syncedTimestamp = Timestamp.from(syncedAt);
        for (AreaReference area : areas) {
            String storedParent = AreaReferenceSupport.normalizeParentCode(area.parentCode());
            jdbcTemplate.update(
                    """
                    INSERT INTO ref_area (
                        area_code,
                        area_name,
                        parent_code,
                        area_level,
                        status,
                        synced_at
                    ) VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        area_name = VALUES(area_name),
                        parent_code = VALUES(parent_code),
                        area_level = VALUES(area_level),
                        status = VALUES(status),
                        synced_at = VALUES(synced_at)
                    """,
                    area.areaCode(),
                    area.areaName(),
                    storedParent.isEmpty() ? null : storedParent,
                    area.level(),
                    STATUS_ACTIVE,
                    syncedTimestamp
            );
        }
    }

    @Override
    public List<String> findRefreshableParentCodes() {
        return jdbcTemplate.query(
                """
                SELECT area_code
                FROM ref_area
                WHERE status = ? AND area_level IN (1, 2)
                ORDER BY area_code
                """,
                (rs, rowNum) -> rs.getString("area_code"),
                STATUS_ACTIVE
        );
    }
}
