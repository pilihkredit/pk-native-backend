package com.pk.infra.repay.repository;

public class RepaymentTrialVaChannelInsertParam {
    private String ownerType;
    private long ownerId;
    private String vaRole;
    private String bankChannel;
    private String instruction;
    private boolean defaultChannel;

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getVaRole() {
        return vaRole;
    }

    public void setVaRole(String vaRole) {
        this.vaRole = vaRole;
    }

    public String getBankChannel() {
        return bankChannel;
    }

    public void setBankChannel(String bankChannel) {
        this.bankChannel = bankChannel;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public boolean isDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(boolean defaultChannel) {
        this.defaultChannel = defaultChannel;
    }
}
