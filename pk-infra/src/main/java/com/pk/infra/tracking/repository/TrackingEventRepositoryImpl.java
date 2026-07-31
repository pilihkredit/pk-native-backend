package com.pk.infra.tracking.repository;

import com.pk.core.tracking.port.TrackingEventRepository;
import com.pk.infra.tracking.mapper.TrackingEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TrackingEventRepositoryImpl implements TrackingEventRepository {
    private final TrackingEventMapper mapper;

    public TrackingEventRepositoryImpl(TrackingEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(TrackingEventInsert event) {
        if (event == null) {
            return;
        }
        mapper.insert(event);
    }
}
