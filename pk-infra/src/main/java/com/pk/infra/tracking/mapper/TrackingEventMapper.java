package com.pk.infra.tracking.mapper;

import com.pk.core.tracking.port.TrackingEventRepository.TrackingEventInsert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrackingEventMapper {
    int insert(TrackingEventInsert event);
}
