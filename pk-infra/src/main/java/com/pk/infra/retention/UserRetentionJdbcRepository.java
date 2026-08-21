package com.pk.infra.retention;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * JDBC access for retention mark/purge jobs.
 * Purge deletes profile + business rows keyed by user_id (and loan/trial children).
 * Missing optional tables (schema drift) are skipped instead of failing the whole user.
 */
public class UserRetentionJdbcRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRetentionJdbcRepository.class);

    private static final RowMapper<UserDeletedRow> USER_DELETED_ROW_MAPPER = (rs, rowNum) -> new UserDeletedRow(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("status")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserRetentionJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserDeletedRow> findPendingDeleted(long afterId, int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, user_id, status
                FROM user_deleted
                WHERE status = 'pending' AND id > ?
                ORDER BY id
                LIMIT ?
                """,
                USER_DELETED_ROW_MAPPER,
                afterId,
                limit
        );
    }

    public boolean userProfileExists(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_profile WHERE id = ?",
                Integer.class,
                userId
        );
        return count != null && count > 0;
    }

    public int updateProfileRetentionUntil(long userId, LocalDateTime retentionUntil) {
        return jdbcTemplate.update(
                "UPDATE user_profile SET retention_until = ? WHERE id = ?",
                Timestamp.valueOf(retentionUntil),
                userId
        );
    }

    public int markDeletedRow(long id, LocalDateTime retentionUntil, LocalDateTime markedAt) {
        return jdbcTemplate.update(
                """
                UPDATE user_deleted
                SET status = 'marked',
                    retention_until = ?,
                    marked_at = ?,
                    fail_reason = NULL
                WHERE id = ? AND status IN ('pending', 'failed')
                """,
                Timestamp.valueOf(retentionUntil),
                Timestamp.valueOf(markedAt),
                id
        );
    }

    public int markDeletedFailed(long id, String failReason) {
        return jdbcTemplate.update(
                """
                UPDATE user_deleted
                SET status = 'failed',
                    fail_reason = ?,
                    retry_count = retry_count + 1
                WHERE id = ?
                """,
                truncate(failReason, 500),
                id
        );
    }

    public List<Long> findUserIdsDueForPurge(LocalDateTime endOfTodayInclusive, long afterId, int limit) {
        return jdbcTemplate.query(
                """
                SELECT id
                FROM user_profile
                WHERE retention_until IS NOT NULL
                  AND retention_until <= ?
                  AND id > ?
                ORDER BY id
                LIMIT ?
                """,
                (rs, rowNum) -> rs.getLong("id"),
                Timestamp.valueOf(endOfTodayInclusive),
                afterId,
                limit
        );
    }

    /**
     * Physically deletes one user and related business/profile rows.
     * Caller should wrap in a transaction.
     */
    public void purgeUserCompletely(long userId) {
        // ---- repayment trial children (no direct user_id) ----
        deleteIgnoreMissing(
                """
                DELETE d FROM repayment_trial_term_discount d
                INNER JOIN repayment_trial_term t ON d.trial_term_id = t.id
                INNER JOIN repayment_trial_order o ON t.trial_order_id = o.id
                INNER JOIN repayment_trial_snapshot s ON o.trial_id = s.id
                WHERE s.user_id = ?
                """,
                userId
        );
        deleteIgnoreMissing(
                """
                DELETE t FROM repayment_trial_term t
                INNER JOIN repayment_trial_order o ON t.trial_order_id = o.id
                INNER JOIN repayment_trial_snapshot s ON o.trial_id = s.id
                WHERE s.user_id = ?
                """,
                userId
        );
        deleteIgnoreMissing(
                """
                DELETE v FROM repayment_trial_va_channel v
                INNER JOIN repayment_trial_snapshot s ON v.owner_type = 'SNAPSHOT' AND v.owner_id = s.id
                WHERE s.user_id = ?
                """,
                userId
        );
        deleteIgnoreMissing(
                """
                DELETE v FROM repayment_trial_va_channel v
                INNER JOIN repayment_trial_order o ON v.owner_type = 'ORDER' AND v.owner_id = o.id
                INNER JOIN repayment_trial_snapshot s ON o.trial_id = s.id
                WHERE s.user_id = ?
                """,
                userId
        );
        deleteIgnoreMissing(
                """
                DELETE o FROM repayment_trial_order o
                INNER JOIN repayment_trial_snapshot s ON o.trial_id = s.id
                WHERE s.user_id = ?
                """,
                userId
        );
        deleteIgnoreMissing("DELETE FROM repayment_trial_snapshot WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM repay_current_order WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM repay_va_snapshot WHERE user_id = ?", userId);

        // ---- loan children without user_id ----
        deleteIgnoreMissing(
                """
                DELETE h FROM loan_status_history h
                INNER JOIN loan_application la ON h.loan_application_id = la.id
                WHERE la.user_id = ?
                """,
                userId
        );
        deleteIgnoreMissing(
                """
                DELETE c FROM contract_file c
                INNER JOIN loan_application la ON c.loan_application_id = la.id
                WHERE la.user_id = ?
                """,
                userId
        );
        deleteIgnoreMissing(
                """
                DELETE p FROM repayment_plan_term p
                INNER JOIN loan_application la ON p.loan_application_id = la.id
                WHERE la.user_id = ?
                """,
                userId
        );

        // ---- loan / credit / lender product (direct user_id) ----
        deleteIgnoreMissing("DELETE FROM loan_lender_bill WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM loan_lender_history_order WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM loan_lender_status_query WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM loan_application WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM loan_quote_term WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM loan_quote WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM pk_lender_product_uneven_rate WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM pk_lender_product_repay_method WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM pk_lender_product WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM pk_lender_product_list WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM credit_lender_status_query WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM credit_application WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_lender_status_query WHERE user_id = ?", userId);

        // ---- external / tracking / callbacks ----
        deleteIgnoreMissing("DELETE FROM external_interaction_callback WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM external_interaction WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM lender_server_event_callback WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM appsflyer_callback WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM tracking_event WHERE user_id = ?", userId);

        // ---- consent / agreement / subject request ----
        deleteIgnoreMissing("DELETE FROM user_consent_record WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_agreement_record WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM data_subject_request WHERE user_id = ?", userId);

        // ---- devices / contact snapshot / messaging logs ----
        deleteIgnoreMissing("DELETE FROM user_device_other_info WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_device WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_contact_snapshot WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM sms_send_log WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM whatsapp_send_log WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM ocr_vendor_call_log WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_mobile_change_face_verification WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_mobile_change_log WHERE user_id = ?", userId);

        // ---- profile satellites ----
        deleteIgnoreMissing("DELETE FROM user_profile_login_log WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_af WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_tongdun WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_bank_card WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_contact WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_contacts WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_identity WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_personal WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM user_profile_version WHERE user_id = ?", userId);

        // ---- app features ----
        deleteIgnoreMissing("DELETE FROM review_guide_exposure WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM review_guide_user_state WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM push_device WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM inbox_user_message WHERE user_id = ?", userId);
        deleteIgnoreMissing("DELETE FROM app_launch_event WHERE user_id = ?", userId);

        // ---- master (must exist) ----
        int profileDeleted = jdbcTemplate.update("DELETE FROM user_profile WHERE id = ?", userId);
        if (profileDeleted != 1) {
            throw new IllegalStateException("user_profile not deleted for userId=" + userId + " (rows=" + profileDeleted + ")");
        }

        jdbcTemplate.update(
                """
                UPDATE user_deleted
                SET status = 'purged',
                    purged_at = CURRENT_TIMESTAMP(3),
                    fail_reason = NULL
                WHERE user_id = ?
                """,
                userId
        );
    }

    public int markPurgeFailed(long userId, String failReason) {
        return jdbcTemplate.update(
                """
                UPDATE user_deleted
                SET status = 'failed',
                    fail_reason = ?,
                    retry_count = retry_count + 1
                WHERE user_id = ?
                """,
                truncate(failReason, 500),
                userId
        );
    }

    /**
     * Skip deletes when the target table (or a join partner) is missing on this DB.
     * Real integrity / constraint errors still propagate.
     */
    private void deleteIgnoreMissing(String sql, Object... args) {
        try {
            jdbcTemplate.update(sql, args);
        } catch (BadSqlGrammarException ex) {
            if (isMissingRelation(ex)) {
                log.warn("Skip purge SQL (missing table/column on this DB): {}", rootMessage(ex));
                return;
            }
            throw ex;
        } catch (DataAccessException ex) {
            if (isMissingRelation(ex)) {
                log.warn("Skip purge SQL (missing table/column on this DB): {}", rootMessage(ex));
                return;
            }
            throw ex;
        }
    }

    private static boolean isMissingRelation(Throwable ex) {
        String msg = rootMessage(ex).toLowerCase(Locale.ROOT);
        return msg.contains("doesn't exist")
                || msg.contains("does not exist")
                || msg.contains("unknown table")
                || msg.contains("unknown column")
                || msg.contains("no such table");
    }

    private static String rootMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage() == null ? String.valueOf(ex.getMessage()) : cur.getMessage();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    public record UserDeletedRow(long id, long userId, String status) {
    }
}
