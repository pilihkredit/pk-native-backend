package com.pk.infra.tracking.mapper;
import com.pk.core.tracking.port.TrackingEventRepository.TrackingEventInsert;
import java.util.Collection; import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface TrackingEventMapper {
    List<String> findExistingEventIds(@Param("eventIds") Collection<String> eventIds);
    int insertBatch(@Param("events") Collection<TrackingEventInsert> events);
}
