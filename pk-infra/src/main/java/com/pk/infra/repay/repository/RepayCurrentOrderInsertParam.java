package com.pk.infra.repay.repository;
import com.pk.core.repay.port.RepayCurrentOrderRepository.CurrentOrderUpsert;
import java.time.Instant;
public class RepayCurrentOrderInsertParam {
    private long id; private long userId; private String currentOrderNo; private long trialId;
    private String repayOrdersJson; private Long couponId; private Instant submittedAt;
    public static RepayCurrentOrderInsertParam from(CurrentOrderUpsert c){RepayCurrentOrderInsertParam p=new RepayCurrentOrderInsertParam();
    p.userId=c.userId();p.currentOrderNo=c.currentOrderNo();p.trialId=c.trialId();p.repayOrdersJson=c.repayOrdersJson();
    p.couponId=c.couponId();p.submittedAt=c.submittedAt();return p;}
    public long getId(){return id;} public void setId(long id){this.id=id;}
    public long getUserId(){return userId;} public String getCurrentOrderNo(){return currentOrderNo;}
    public long getTrialId(){return trialId;} public String getRepayOrdersJson(){return repayOrdersJson;}
    public Long getCouponId(){return couponId;} public Instant getSubmittedAt(){return submittedAt;}
}
