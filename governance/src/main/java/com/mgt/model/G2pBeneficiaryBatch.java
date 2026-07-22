package com.mgt.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "g2p_beneficiary_batch")
public class G2pBeneficiaryBatch {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="batch_ref",     unique=true, nullable=false) private String batchRef;
    @Column(name="cycle_month",   nullable=false)              private String cycleMonth;
    @Column(name="ward")                                       private String ward;
    @Column(name="district")                                   private String district;
    @Column(name="total_farmers")                              private int totalFarmers;
    @Column(name="total_amount")                               private BigDecimal totalAmount = BigDecimal.ZERO;
    @Column(name="amount_per_farmer")                          private BigDecimal amountPerFarmer = BigDecimal.ZERO;
    @Column(name="gateway")                                    private String gateway = "BEFTN";
    @Column(name="status")                                     private String status  = "DRAFT";
    @Column(name="submitted_by")                               private String submittedBy;
    @Column(name="submitted_at")                               private LocalDateTime submittedAt;
    @Column(name="completed_at")                               private LocalDateTime completedAt;
    @Column(name="remarks", columnDefinition="TEXT")           private String remarks;
    @Column(name="created_at")                                 private LocalDateTime createdAt = LocalDateTime.now();

    public int getId() { return id; }
    public String getBatchRef() { return batchRef; }
    public void setBatchRef(String v) { this.batchRef=v; }
    public String getCycleMonth() { return cycleMonth; }
    public void setCycleMonth(String v) { this.cycleMonth=v; }
    public String getWard() { return ward; }
    public void setWard(String v) { this.ward=v; }
    public String getDistrict() { return district; }
    public void setDistrict(String v) { this.district=v; }
    public int getTotalFarmers() { return totalFarmers; }
    public void setTotalFarmers(int v) { this.totalFarmers=v; }
    public BigDecimal getTotalAmount() { return totalAmount!=null?totalAmount:BigDecimal.ZERO; }
    public void setTotalAmount(BigDecimal v) { this.totalAmount=v; }
    public BigDecimal getAmountPerFarmer() { return amountPerFarmer!=null?amountPerFarmer:BigDecimal.ZERO; }
    public void setAmountPerFarmer(BigDecimal v) { this.amountPerFarmer=v; }
    public String getGateway() { return gateway; }
    public void setGateway(String v) { this.gateway=v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status=v; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String v) { this.submittedBy=v; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime v) { this.submittedAt=v; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime v) { this.completedAt=v; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String v) { this.remarks=v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt=v; }
}
