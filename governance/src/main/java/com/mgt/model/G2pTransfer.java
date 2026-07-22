package com.mgt.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "g2p_transfer")
public class G2pTransfer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="batch_id",       nullable=false) private int batchId;
    @Column(name="card_id",        nullable=false) private int cardId;
    @Column(name="card_no")                        private String cardNo;
    @Column(name="farmer_name")                    private String farmerName;
    @Column(name="nid")                            private String nid;
    @Column(name="mobile")                         private String mobile;
    @Column(name="bank_name")                      private String bankName;
    @Column(name="bank_account")                   private String bankAccount;
    @Column(name="bank_branch")                    private String bankBranch;
    @Column(name="amount",         nullable=false) private BigDecimal amount;
    @Column(name="gateway",        nullable=false) private String gateway;
    @Column(name="txn_ref",        unique=true)    private String txnRef;
    @Column(name="provider_txn_id")                private String providerTxnId;
    @Column(name="status")                         private String status = "PENDING";
    @Column(name="failure_reason", columnDefinition="TEXT") private String failureReason;
    @Column(name="initiated_at")                   private LocalDateTime initiatedAt;
    @Column(name="completed_at")                   private LocalDateTime completedAt;
    @Column(name="retry_count")                    private int retryCount = 0;
    @Column(name="created_at")                     private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name="updated_at")                     private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public int getId() { return id; }
    public int getBatchId() { return batchId; }
    public void setBatchId(int v) { this.batchId=v; }
    public int getCardId() { return cardId; }
    public void setCardId(int v) { this.cardId=v; }
    public String getCardNo() { return cardNo; }
    public void setCardNo(String v) { this.cardNo=v; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String v) { this.farmerName=v; }
    public String getNid() { return nid; }
    public void setNid(String v) { this.nid=v; }
    public String getMobile() { return mobile; }
    public void setMobile(String v) { this.mobile=v; }
    public String getBankName() { return bankName; }
    public void setBankName(String v) { this.bankName=v; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String v) { this.bankAccount=v; }
    public String getBankBranch() { return bankBranch; }
    public void setBankBranch(String v) { this.bankBranch=v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount=v; }
    public String getGateway() { return gateway; }
    public void setGateway(String v) { this.gateway=v; }
    public String getTxnRef() { return txnRef; }
    public void setTxnRef(String v) { this.txnRef=v; }
    public String getProviderTxnId() { return providerTxnId; }
    public void setProviderTxnId(String v) { this.providerTxnId=v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status=v; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String v) { this.failureReason=v; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(LocalDateTime v) { this.initiatedAt=v; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime v) { this.completedAt=v; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int v) { this.retryCount=v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
