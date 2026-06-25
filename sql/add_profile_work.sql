CREATE TABLE IF NOT EXISTS user_profile_work (
    profile_id BIGINT UNSIGNED NOT NULL COMMENT 'User profile identifier',
    industry INT NOT NULL COMMENT 'Industry code',
    company_name VARCHAR(128) NOT NULL COMMENT 'Company name',
    work_province_code VARCHAR(32) NOT NULL COMMENT 'Work location province code',
    work_city_code VARCHAR(32) NOT NULL COMMENT 'Work location city code',
    work_district_code VARCHAR(32) NOT NULL COMMENT 'Work location district code',
    work_address VARCHAR(512) NOT NULL COMMENT 'Work street address',
    income VARCHAR(16) NOT NULL COMMENT 'Monthly income as numeric string',
    payday INT NOT NULL COMMENT 'Payday 1-31',
    profession_degree INT NOT NULL COMMENT 'Profession type code',
    module_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Onboarding module status',
    last_request_id VARCHAR(64) NOT NULL COMMENT 'Last successful request id',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Record creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Record update time',
    PRIMARY KEY (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User work information module';
