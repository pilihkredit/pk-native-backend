package com.pk.infra.callback.mapper;
import com.pk.infra.callback.repository.CallbackEventInsertParam;
import com.pk.core.callback.port.CallbackEventRepository.CallbackEventRecord;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface CallbackEventMapper {
    int insert(CallbackEventInsertParam param);
    Long findIdByCallbackNo(@Param("callbackNo") String callbackNo);
    CallbackEventRecord findById(@Param("id") long id);
    CallbackEventRecord findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    int markProcessed(@Param("id") long id, @Param("processStatus") String processStatus, @Param("processedAt") Instant processedAt);
    int markIgnored(@Param("id") long id, @Param("processStatus") String processStatus, @Param("processedAt") Instant processedAt);
}
