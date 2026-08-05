-- Review guide state and exposure records.
-- Compatible with MySQL 5.7 / 8.0. Safe to re-run.

CREATE TABLE IF NOT EXISTS review_guide_user_state (
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
    real_review_clicked_at DATETIME(3) NULL COMMENT 'Set when ORDER_CREATED/LOAN_PAID rating >= minJumpRating; suppresses all scenes',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Record update time',
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Per-user review guide state';

CREATE TABLE IF NOT EXISTS review_guide_exposure (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'user_profile.id',
    scene VARCHAR(32) NOT NULL COMMENT 'CREDIT_FAILED, ORDER_CREATED, or LOAN_PAID',
    guide_type VARCHAR(16) NOT NULL COMMENT 'IN_APP (legacy FAKE/REAL ignored)',
    shown_at DATETIME(3) NOT NULL COMMENT 'Time the display eligibility was claimed',
    in_app_rating TINYINT UNSIGNED NULL COMMENT 'In-app rating from 1 to 5',
    feedback_at DATETIME(3) NULL COMMENT 'In-app rating submission time',
    clicked_at DATETIME(3) NULL COMMENT 'Unused after store-jump redesign',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'Record update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_guide_exposure_user_scene (user_id, scene),
    KEY idx_review_guide_exposure_user_type_clicked (user_id, guide_type, clicked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Review guide exposure and action records';
