package com.pk.infra.auth.mapper;

import com.pk.core.auth.port.UserMobileChangeLogRepository.UserMobileChangeLogEntry;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMobileChangeLogMapper {
    int insert(UserMobileChangeLogEntry entry);
}
