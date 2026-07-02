package com.pk.infra.tracking.repository;
import com.pk.core.tracking.port.TrackingEventRepository;
import com.pk.infra.tracking.mapper.TrackingEventMapper;
import java.util.Collection; import java.util.HashSet; import java.util.Set;
import org.springframework.stereotype.Repository;
@Repository public class TrackingEventRepositoryImpl implements TrackingEventRepository {
private final TrackingEventMapper mapper; public TrackingEventRepositoryImpl(TrackingEventMapper mapper){this.mapper=mapper;}
@Override public Set<String> findExistingEventIds(Collection<String> eventIds){
    if(eventIds==null||eventIds.isEmpty()) return Set.of(); return new HashSet<>(mapper.findExistingEventIds(eventIds));}
@Override public void insertBatch(Collection<TrackingEventInsert> events){ if(events==null||events.isEmpty()) return; mapper.insertBatch(events);}}
