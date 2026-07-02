package com.pk.infra.loan.repository;
import com.pk.core.loan.port.ProductSnapshotRepository;
import com.pk.infra.loan.mapper.ProductSnapshotMapper;
import java.util.Optional; import org.springframework.stereotype.Repository;
@Repository public class ProductSnapshotRepositoryImpl implements ProductSnapshotRepository {
private final ProductSnapshotMapper mapper; public ProductSnapshotRepositoryImpl(ProductSnapshotMapper mapper){this.mapper=mapper;}
@Override public ProductSnapshotRecord insert(ProductSnapshotInsert command){
    ProductSnapshotInsertParam p=ProductSnapshotInsertParam.from(command); mapper.insert(p);
    return new ProductSnapshotRecord(p.getId(),command.snapshotNo(),command.applyId(),command.creditApplicationId(),
        command.creditStatus(),command.productStatus(),command.productsJson(),command.fetchedAt());}
@Override public Optional<ProductSnapshotRecord> findBySnapshotNo(String snapshotNo){return Optional.ofNullable(mapper.findBySnapshotNo(snapshotNo));}
@Override public Optional<ProductSnapshotRecord> findLatestByCreditApplicationId(long creditApplicationId){
    return Optional.ofNullable(mapper.findLatestByCreditApplicationId(creditApplicationId));}}
