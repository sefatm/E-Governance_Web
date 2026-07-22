package com.mgt.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * LPG cylinder stock per cycle.
 * TcbStock এর মতো প্যাটার্ন।
 * Table: lpg_stock
 */
@Entity
@Table(name = "lpg_stock")
public class LpgStock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "cycle_month",  nullable = false) private String cycleMonth;
    @Column(name = "batch_label")                    private String batchLabel;
    @Column(name = "ward")                           private String ward;
    @Column(name = "dealer_name")                    private String dealerName;
    @Column(name = "dealer_code")                    private String dealerCode;
    @Column(name = "cylinder_size")                  private String cylinderSize = "12kg";

    // মজুদ
    @Column(name = "total_cylinders", nullable = false) private int totalCylinders = 0;

    // বিতরণ হওয়া — deductStock() UPDATE query-তে কমানো হয়
    // Integer (wrapper) — TcbStock এর distributed এর মতো NULL-safe
    @Column(name = "distributed")                    private Integer distributed = 0;

    @Column(name = "total_cards")                    private int totalCards = 0;

    @Column(name = "created_at")                     private LocalDateTime createdAt;
    @Column(name = "updated_at")                     private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (distributed    == null) distributed    = 0;
        if (totalCylinders < 0)    totalCylinders  = 0;
        if (createdAt      == null) createdAt       = LocalDateTime.now();
        if (updatedAt      == null) updatedAt       = LocalDateTime.now();
    }

    // ── Computed (transient) ───────────────────────────────────
    @Transient
    public int getRemaining() {
        return Math.max(0, totalCylinders - (distributed != null ? distributed : 0));
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int getId()                          { return id; }
    public String getCycleMonth()               { return cycleMonth; }
    public void setCycleMonth(String v)         { this.cycleMonth = v; }
    public String getBatchLabel()               { return batchLabel; }
    public void setBatchLabel(String v)         { this.batchLabel = v; }
    public String getWard()                     { return ward; }
    public void setWard(String v)               { this.ward = v; }
    public String getDealerName()               { return dealerName; }
    public void setDealerName(String v)         { this.dealerName = v; }
    public String getDealerCode()               { return dealerCode; }
    public void setDealerCode(String v)         { this.dealerCode = v; }
    public String getCylinderSize()             { return cylinderSize; }
    public void setCylinderSize(String v)       { this.cylinderSize = v; }
    public int getTotalCylinders()              { return totalCylinders; }
    public void setTotalCylinders(int v)        { this.totalCylinders = v; }
    public int getDistributed()                 { return distributed != null ? distributed : 0; }
    public void setDistributed(Integer v)       { this.distributed = v != null ? v : 0; }
    public int getTotalCards()                  { return totalCards; }
    public void setTotalCards(int v)            { this.totalCards = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }
}
