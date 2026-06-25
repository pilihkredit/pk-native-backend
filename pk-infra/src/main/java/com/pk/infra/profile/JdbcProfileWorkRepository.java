package com.pk.infra.profile;

import com.pk.core.profile.ProfileWorkData;
import com.pk.core.profile.port.ProfileWorkRepository;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProfileWorkRepository implements ProfileWorkRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProfileWorkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProfileWorkData> findByProfileId(long profileId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    """
                    SELECT profile_id, industry, company_name, work_province_code, work_city_code,
                           work_district_code, work_address, income, payday, profession_degree,
                           module_status, last_request_id
                    FROM user_profile_work
                    WHERE profile_id = ?
                    """,
                    (rs, rowNum) -> new ProfileWorkData(
                            rs.getLong("profile_id"),
                            rs.getInt("industry"),
                            rs.getString("company_name"),
                            rs.getString("work_province_code"),
                            rs.getString("work_city_code"),
                            rs.getString("work_district_code"),
                            rs.getString("work_address"),
                            rs.getString("income"),
                            rs.getInt("payday"),
                            rs.getInt("profession_degree"),
                            rs.getString("module_status"),
                            rs.getString("last_request_id")
                    ),
                    profileId
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void upsert(ProfileWorkData data) {
        jdbcTemplate.update(
                """
                INSERT INTO user_profile_work (
                    profile_id,
                    industry,
                    company_name,
                    work_province_code,
                    work_city_code,
                    work_district_code,
                    work_address,
                    income,
                    payday,
                    profession_degree,
                    module_status,
                    last_request_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    industry = VALUES(industry),
                    company_name = VALUES(company_name),
                    work_province_code = VALUES(work_province_code),
                    work_city_code = VALUES(work_city_code),
                    work_district_code = VALUES(work_district_code),
                    work_address = VALUES(work_address),
                    income = VALUES(income),
                    payday = VALUES(payday),
                    profession_degree = VALUES(profession_degree),
                    module_status = VALUES(module_status),
                    last_request_id = VALUES(last_request_id)
                """,
                data.profileId(),
                data.industry(),
                data.companyName(),
                data.workProvinceCode(),
                data.workCityCode(),
                data.workDistrictCode(),
                data.workAddress(),
                data.income(),
                data.payday(),
                data.professionDegree(),
                data.moduleStatus(),
                data.lastRequestId()
        );
    }
}
