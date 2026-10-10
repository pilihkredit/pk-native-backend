package com.pk.infra.push.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.PushNotificationTask;
import com.pk.core.push.port.PushNotificationTaskRepository;
import com.pk.infra.push.mapper.PushNotificationTaskMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class PushNotificationTaskRepositoryImpl implements PushNotificationTaskRepository {
    private final PushNotificationTaskMapper mapper;
    private final ObjectMapper objectMapper;

    public PushNotificationTaskRepositoryImpl(PushNotificationTaskMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<PushNotificationTask> findById(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::toDomain);
    }

    @Override
    public List<PushNotificationTask> findReadyToDeliver(int limit) {
        return mapper.findReadyToDeliver(limit).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean tryMarkSending(long id) {
        return mapper.tryMarkSending(id) > 0;
    }

    @Override
    public void markCompleted(
            long id,
            int totalCount,
            int successCount,
            int failureCount,
            Instant completedAt
    ) {
        mapper.markCompleted(id, totalCount, successCount, failureCount, completedAt);
    }

    @Override
    public void markFailed(long id, String statusMessage, Instant failedAt) {
        mapper.markFailed(id, statusMessage, failedAt);
    }

    @Override
    public int markCleared(List<Long> taskIds, Instant clearedAt) {
        if (taskIds == null || taskIds.isEmpty()) {
            return 0;
        }
        return mapper.markCleared(taskIds, clearedAt);
    }

    private PushNotificationTask toDomain(PushNotificationTaskRow row) {
        return new PushNotificationTask(
                row.id(),
                row.taskName(),
                row.title(),
                row.body(),
                row.buttonText(),
                row.pushType(),
                row.requiredRead() == 1,
                parseStringList(row.pushPagesJson()),
                row.internalUrl(),
                row.targetUrl(),
                row.externalUrl(),
                row.bannerUrl(),
                row.audienceType(),
                row.audienceFileName(),
                parseLongList(row.clearTargetIdsJson()),
                row.notes(),
                row.status(),
                row.statusMessage(),
                row.totalCount(),
                row.successCount(),
                row.failureCount(),
                row.clearedAt(),
                row.publishedAt(),
                row.sendCompletedAt()
        );
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "invalid push_pages json");
        }
    }

    private List<Long> parseLongList(String json) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (Exception exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "invalid clear_target_ids json");
        }
    }

    public static String writeJson(ObjectMapper objectMapper, Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "invalid json column value");
        }
    }

    public static List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
    }
}
