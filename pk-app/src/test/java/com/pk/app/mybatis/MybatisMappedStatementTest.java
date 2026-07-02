package com.pk.app.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

class MybatisMappedStatementTest {
    private static final String USER_AUTH_FIND_BY_PROFILE_ID =
            "com.pk.infra.auth.mapper.UserAuthMapper.findByProfileId";
    private static final String USER_PROFILE_SUMMARY_MAP =
            "com.pk.infra.auth.mapper.UserAuthMapper.userProfileSummaryMap";
    private static final String INFRA_MAPPER_CLASSES = "classpath*:com/pk/infra/**/mapper/*Mapper.class";

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

    @Test
    void allMapperInterfaceMethodsHaveMappedStatements() {
        contextRunner.run(context -> {
            SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);

            List<String> missingStatements = loadMapperTypes().stream()
                    .flatMap(mapperType -> List.of(mapperType.getDeclaredMethods()).stream()
                            .filter(method -> !method.isBridge())
                            .filter(method -> !method.isSynthetic())
                            .filter(method -> !Modifier.isStatic(method.getModifiers()))
                            .map(method -> statementId(mapperType, method)))
                    .filter(statementId -> !sqlSessionFactory.getConfiguration().hasStatement(statementId))
                    .toList();

            assertThat(missingStatements).isEmpty();
        });
    }

    @Test
    void allConstructorResultMapsMatchDeclaredConstructors() {
        contextRunner.run(context -> {
            SqlSessionFactory sqlSessionFactory = context.getBean(SqlSessionFactory.class);

            List<String> invalidResultMaps = new ArrayList<>();
            for (ResultMap resultMap : sqlSessionFactory.getConfiguration().getResultMaps()) {
                if (resultMap.getConstructorResultMappings().isEmpty()) {
                    continue;
                }
                Class<?>[] constructorTypes = resultMap.getConstructorResultMappings().stream()
                        .map(mapping -> mapping.getJavaType())
                        .toArray(Class<?>[]::new);
                try {
                    resultMap.getType().getDeclaredConstructor(constructorTypes);
                } catch (NoSuchMethodException exception) {
                    invalidResultMaps.add(resultMap.getId() + " -> " + resultMap.getType().getName()
                            + constructorSignature(constructorTypes));
                }
            }

            assertThat(invalidResultMaps).isEmpty();
        });
    }

    static class TestDataSourceConfiguration {
        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }
    }

    private static List<Class<?>> loadMapperTypes() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
            Resource[] resources = resolver.getResources(INFRA_MAPPER_CLASSES);
            List<Class<?>> mapperTypes = new ArrayList<>();
            for (Resource resource : resources) {
                String className = readerFactory.getMetadataReader(resource)
                        .getClassMetadata()
                        .getClassName();
                mapperTypes.add(Class.forName(className));
            }
            return mapperTypes;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load mapper interfaces", exception);
        }
    }

    private static String statementId(Class<?> mapperType, Method method) {
        return mapperType.getName() + "." + method.getName();
    }

    private static String constructorSignature(Class<?>[] constructorTypes) {
        return List.of(constructorTypes).stream()
                .map(Class::getName)
                .toList()
                .toString();
    }
}
