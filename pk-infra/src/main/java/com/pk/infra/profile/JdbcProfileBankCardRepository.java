package com.pk.infra.profile;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.port.ProfileBankCardRepository;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfileBankCardRepository implements ProfileBankCardRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProfileBankCardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProfileBankCardData> findByProfileId(long profileId) {
        try {
            return Optional.of(queryOne("WHERE profile_id = ?", profileId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProfileBankCardData> findByCardNoHash(String cardNoHash) {
        try {
            return Optional.of(queryOne("WHERE card_no_hash = ?", cardNoHash));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void upsert(ProfileBankCardData data) {
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_bank_card (
                    profile_id,
                    bank_code,
                    card_no_hash,
                    card_no_ciphertext,
                    card_no_nonce,
                    card_no_tag,
                    verify_status,
                    verify_error_code,
                    default_flag,
                    module_status,
                    last_request_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?)
                ON DUPLICATE KEY UPDATE
                    bank_code = VALUES(bank_code),
                    card_no_hash = VALUES(card_no_hash),
                    card_no_ciphertext = VALUES(card_no_ciphertext),
                    card_no_nonce = VALUES(card_no_nonce),
                    card_no_tag = VALUES(card_no_tag),
                    verify_status = VALUES(verify_status),
                    verify_error_code = VALUES(verify_error_code),
                    default_flag = VALUES(default_flag),
                    module_status = VALUES(module_status),
                    last_request_id = VALUES(last_request_id)
                """,
                data.profileId(),
                data.bankCode(),
                data.cardNoHash(),
                data.cardNumber().ciphertextBase64(),
                data.cardNumber().nonce(),
                data.cardNumber().tag(),
                data.verifyStatus(),
                data.verifyErrorCode(),
                data.moduleStatus(),
                data.lastRequestId()
        );
    }

    @Override
    public void updateLastLenderAudit(long profileId, String requestDataJson, String responseDataJson) {
        jdbcTemplate.update(
                """
                UPDATE user_profile_bank_card
                SET last_lender_request_json = ?,
                    last_lender_response_json = ?
                WHERE profile_id = ?
                """,
                requestDataJson,
                responseDataJson,
                profileId
        );
    }

    private ProfileBankCardData queryOne(String predicate, Object arg) {
        return jdbcTemplate.queryForObject(
                """
                SELECT profile_id, bank_code, card_no_hash, card_no_ciphertext, card_no_nonce, card_no_tag,
                       verify_status, verify_error_code, module_status, last_request_id,
                       last_lender_request_json, last_lender_response_json
                FROM user_profile_bank_card
                """ + predicate,
                (rs, rowNum) -> new ProfileBankCardData(
                        rs.getLong("profile_id"),
                        rs.getString("bank_code"),
                        new EncryptedField(
                                rs.getString("card_no_ciphertext"),
                                rs.getBytes("card_no_nonce"),
                                rs.getBytes("card_no_tag")
                        ),
                        rs.getString("card_no_hash"),
                        rs.getString("verify_status"),
                        rs.getString("verify_error_code"),
                        rs.getString("module_status"),
                        rs.getString("last_request_id"),
                        rs.getString("last_lender_request_json"),
                        rs.getString("last_lender_response_json")
                ),
                arg
        );
    }
}
