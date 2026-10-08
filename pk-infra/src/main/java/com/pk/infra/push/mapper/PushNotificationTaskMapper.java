package com.pk.infra.push.mapper;

import com.pk.infra.push.repository.PushNotificationTaskRow;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PushNotificationTaskMapper {

    PushNotificationTaskRow findById(@Param("id") long id);

    List<PushNotificationTaskRow> findReadyToDeliver(@Param("limit") int limit);

    List<PushNotificationTaskRow> findPendingRequiredForUser(
            @Param("userId") long userId,
            @Param("limit") int limit
    );

    int tryMarkSending(@Param("id") long id);

    int markCompleted(
            @Param("id") long id,
            @Param("totalCount") int totalCount,
            @Param("successCount") int successCount,
            @Param("failureCount") int failureCount,
            @Param("completedAt") Instant completedAt
    );

    int markFailed(@Param("id") long id, @Param("statusMessage") String statusMessage,
            @Param("failedAt") Instant failedAt);

    int markCleared(@Param("taskIds") List<Long> taskIds, @Param("clearedAt") Instant clearedAt);
}
