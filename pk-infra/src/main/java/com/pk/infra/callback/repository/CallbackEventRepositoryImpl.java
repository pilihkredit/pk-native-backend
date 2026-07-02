package com.pk.infra.callback.repository;
import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.infra.callback.mapper.CallbackEventMapper;
import java.time.Instant; import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository public class CallbackEventRepositoryImpl implements CallbackEventRepository {
private final CallbackEventMapper mapper; public CallbackEventRepositoryImpl(CallbackEventMapper mapper){this.mapper=mapper;}
@Override public long insert(CallbackEventInsert insert){
    CallbackEventInsertParam p=CallbackEventInsertParam.from(insert); mapper.insert(p);
    Long id=mapper.findIdByCallbackNo(insert.callbackNo());
    if(id==null) throw new IllegalStateException("Failed to load inserted callback event"); return id;}
@Override public Optional<CallbackEventRecord> findById(long id){return Optional.ofNullable(mapper.findById(id));}
@Override public Optional<CallbackEventRecord> findByIdempotencyKey(String idempotencyKey){return Optional.ofNullable(mapper.findByIdempotencyKey(idempotencyKey));}
@Override public void markProcessed(long id,Instant processedAt){mapper.markProcessed(id,CallbackProcessStatus.PROCESSED,processedAt);}
@Override public void markIgnored(long id,Instant processedAt){mapper.markIgnored(id,CallbackProcessStatus.IGNORED,processedAt);}}
