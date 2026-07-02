package com.pk.app.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Bean;

class MybatisMappedStatementTest {
    private static final String USER_AUTH_FIND_BY_PROFILE_ID =
            "com.pk.infra.auth.mapper.UserAuthMapper.findByProfileId";
    private static final String USER_PROFILE_SUMMARY_MAP =
            "com.pk.infra.auth.mapper.UserAuthMapper.userProfileSummaryMap";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withConfiguration(AutoConfigurations.of(MybatisAutoConfiguration.class))
            .withUserConfiguration(TestDataSourceConfiguration.class);

    @Test
    void loadsInfraMapperXmlStatements() {
        contextRunner.run(context -> {
            SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);

            assertThat(sqlSessionFactory.getConfiguration().hasStatement(USER_AUTH_FIND_BY_PROFILE_ID)).isTrue();
            ResultMap resultMap = sqlSessionFactory.getConfiguration().getResultMap(USER_PROFILE_SUMMARY_MAP);
            List<? extends Class<?>> constructorTypes = resultMap.getConstructorResultMappings().stream()
                    .map(mapping -> mapping.getJavaType())
                    .toList();
            assertThat(resultMap.getConstructorResultMappings())
                    .hasSize(4);
            assertThat(constructorTypes.toArray())
                    .containsExactly(long.class, String.class, String.class, boolean.class);
        });
    }

    static class TestDataSourceConfiguration {
        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }
    }
}
