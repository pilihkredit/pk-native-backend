package com.pk.infra.reference.repository;
import com.pk.core.reference.BankReference; import com.pk.core.reference.port.RefBankRepository;
import com.pk.infra.reference.mapper.RefBankMapper;
import java.time.Instant; import java.util.List; import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository public class RefBankRepositoryImpl implements RefBankRepository {
    static final String STATUS_ACTIVE="ACTIVE";
    private final RefBankMapper mapper; public RefBankRepositoryImpl(RefBankMapper mapper){this.mapper=mapper;}
    @Override public List<BankReference> findAllActive(){return mapper.findAllActive(STATUS_ACTIVE);}
    @Override public Optional<Instant> findLatestSyncedAt(){return Optional.ofNullable(mapper.findLatestSyncedAt(STATUS_ACTIVE));}
    @Override public boolean existsActiveBankCode(String bankCode){return mapper.countActiveByBankCode(STATUS_ACTIVE,bankCode)>0;}
    @Override public void replaceAll(List<BankReference> banks,Instant syncedAt){
        mapper.deactivateAll(STATUS_ACTIVE);
        for(BankReference bank:banks) mapper.upsertBank(new RefBankUpsertParam(bank,STATUS_ACTIVE,syncedAt));}}
