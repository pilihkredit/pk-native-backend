package com.pk.infra.loan.repository;
import com.pk.core.loan.port.ProductSnapshotRepository.ProductSnapshotInsert;
import java.time.Instant;
public class ProductSnapshotInsertParam {
    private long id; private String snapshotNo; private String applyId; private long creditApplicationId;
    private String creditStatus; private String productStatus; private String productsJson; private Instant fetchedAt;
    public static ProductSnapshotInsertParam from(ProductSnapshotInsert c){
        ProductSnapshotInsertParam p=new ProductSnapshotInsertParam();
        p.snapshotNo=c.snapshotNo();p.applyId=c.applyId();p.creditApplicationId=c.creditApplicationId();
        p.creditStatus=c.creditStatus();p.productStatus=c.productStatus();p.productsJson=c.productsJson();p.fetchedAt=c.fetchedAt();return p;}
    public long getId(){return id;} public void setId(long id){this.id=id;}
    public String getSnapshotNo(){return snapshotNo;} public String getApplyId(){return applyId;}
    public long getCreditApplicationId(){return creditApplicationId;} public String getCreditStatus(){return creditStatus;}
    public String getProductStatus(){return productStatus;} public String getProductsJson(){return productsJson;}
    public Instant getFetchedAt(){return fetchedAt;}
}
