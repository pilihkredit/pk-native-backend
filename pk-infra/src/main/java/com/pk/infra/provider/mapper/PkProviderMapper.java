package com.pk.infra.provider.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PkProviderMapper {
    String findActiveProviderCode(@Param("providerCode") String providerCode);
}
