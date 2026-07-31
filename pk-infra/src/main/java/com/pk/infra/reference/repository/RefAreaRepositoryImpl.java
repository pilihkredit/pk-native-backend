package com.pk.infra.reference.repository;
import com.pk.core.reference.AreaReference; import com.pk.core.reference.port.RefAreaRepository;
import com.pk.infra.reference.AreaReferenceSupport; import com.pk.infra.reference.mapper.RefAreaMapper;
import java.time.Instant; import java.util.List; import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository public class RefAreaRepositoryImpl implements RefAreaRepository {
    static final String STATUS_ACTIVE="ACTIVE";
    private final RefAreaMapper mapper; public RefAreaRepositoryImpl(RefAreaMapper mapper){this.mapper=mapper;}
    @Override public List<AreaReference> findActiveByParentCode(String parentCode){
        String n=AreaReferenceSupport.normalizeParentCode(parentCode);
        return mapper.findActiveByParentCode(STATUS_ACTIVE,n).stream().map(a->new AreaReference(a.areaCode(),a.areaName(),AreaReferenceSupport.normalizeParentCode(a.parentCode()),a.level())).toList();}
    @Override public Optional<Instant> findLatestSyncedAtByParent(String parentCode){
        return Optional.ofNullable(mapper.findLatestSyncedAtByParent(STATUS_ACTIVE,AreaReferenceSupport.normalizeParentCode(parentCode)));}
    @Override public Optional<AreaReference> findActiveByCode(String areaCode){
        AreaReference row=mapper.findActiveByCode(STATUS_ACTIVE,areaCode);
        return row==null?Optional.empty():Optional.of(new AreaReference(row.areaCode(),row.areaName(),AreaReferenceSupport.normalizeParentCode(row.parentCode()),row.level()));}
    @Override public boolean existsActiveAreaCode(String areaCode){return mapper.countActiveByAreaCode(STATUS_ACTIVE,areaCode)>0;}
    @Override public void replaceChildrenByParent(String parentCode,List<AreaReference> areas,Instant syncedAt){
        String n=AreaReferenceSupport.normalizeParentCode(parentCode); mapper.deactivateChildrenByParent(STATUS_ACTIVE,n);
        for(AreaReference area:areas){String stored=AreaReferenceSupport.normalizeParentCode(area.parentCode());
            mapper.upsertArea(new RefAreaUpsertParam(area.areaCode(),area.areaName(),stored.isEmpty()?null:stored,area.level(),STATUS_ACTIVE,syncedAt));}}
    @Override public List<String> findRefreshableParentCodes(){return mapper.findRefreshableParentCodes(STATUS_ACTIVE);}}
