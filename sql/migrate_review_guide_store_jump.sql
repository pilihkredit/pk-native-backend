-- Align review-guide with in-app rating + store-jump threshold from app_config.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

INSERT INTO app_config (`key`, `value`)
VALUES (
    'reviewGuide.minJumpRating',
    CAST('4' AS JSON)
)
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`);

-- review_guide_user_state.real_review_clicked_at now means:
-- user submitted rating >= minJumpRating on ORDER_CREATED or LOAN_PAID
-- (frontend jumps to store); afterwards claim never shows again for any scene.
