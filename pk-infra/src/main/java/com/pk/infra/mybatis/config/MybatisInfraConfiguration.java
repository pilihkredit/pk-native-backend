package com.pk.infra.mybatis.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.pk.infra.**.mapper")
public class MybatisInfraConfiguration {
}
