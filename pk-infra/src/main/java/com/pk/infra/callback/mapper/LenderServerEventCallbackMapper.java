package com.pk.infra.callback.mapper;

import com.pk.core.callback.port.LenderServerEventCallbackRepository.LenderServerEventCallbackRecord;
import com.pk.infra.callback.repository.LenderServerEventCallbackInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LenderServerEventCallbackMapper {
    int insert(LenderServerEventCallbackInsertParam param);

    LenderServerEventCallbackRecord findByEventId(@Param("eventId") String eventId);
}
