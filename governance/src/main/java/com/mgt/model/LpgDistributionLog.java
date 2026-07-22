package com.mgt.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LPG cylinder collection history per card per cycle.
 * Table: lpg_distribution_log
 */
@Entity
@Table(name = "lpg_distribution_log")
public class LpgDistributionLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "card_id",       nullable = false) private int    cardId;
    @Column(name = "card_no")                         private String cardNo;
    @Column(name = "holder_name")                     private String holderName;
    @Column(name = "nid")                             private String nid;
    @Column(name = "contact")                         private String contact;
    @Column(name = "ward")                            private String ward;
    @Column(name = "district")                        private String district;

    @Column(name = "cycle_month",   nullable = false) private String cycleMonth;   // YYYY-MM
    @Column(name = "cylinders_qty", nullable = false) private int    cylindersQty = 1;
    @Column(name = "cylinder_size")                   private String cylinderSize  = "12kg";
    @Column(name = "dealer_name")                     private String dealerName;
    @Column(name = "dealer_code")                     private String dealerCode;
    @Column(name = "collected_by")                    private String collectedBy;   // dealer/operator name
    @Column(name = "dist_date")                       private LocalDate distDate;

    // stock deduct reference
    @Column(name = "stock_id")                        private Integer stockId;

    @Column(name = "remarks", columnDefinition = "TEXT") private String remarks;
    @Column(name = "created_at")                      private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (distDate  == null) distDate  = LocalDate.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (cylindersQty <= 0) cylindersQty = 1;
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int getId()                          { return id; }
    public int getCardId()                      { return cardId; }
    public void setCardId(int v)                { this.cardId = v; }
    public String getCardNo()                   { return cardNo; }
    public void setCardNo(String v)             { this.cardNo = v; }
    public String getHolderName()               { return holderName; }
    public void setHolderName(String v)         { this.holderName = v; }
    public String getNid()                      { return nid; }
    public void setNid(String v)                { this.nid = v; }
    public String getContact()                  { return contact; }
    public void setContact(String v)            { this.contact = v; }
    public String getWard()                     { return ward; }
    public void setWard(String v)               { this.ward = v; }
    public String getDistrict()                 { return district; }
    public void setDistrict(String v)           { this.district = v; }
    public String getCycleMonth()               { return cycleMonth; }
    public void setCycleMonth(String v)         { this.cycleMonth = v; }
    public int getCylindersQty()                { return cylindersQty; }
    public void setCylindersQty(int v)          { this.cylindersQty = v; }
    public String getCylinderSize()             { return cylinderSize; }
    public void setCylinderSize(String v)       { this.cylinderSize = v; }
    public String getDealerName()               { return dealerName; }
    public void setDealerName(String v)         { this.dealerName = v; }
    public String getDealerCode()               { return dealerCode; }
    public void setDealerCode(String v)         { this.dealerCode = v; }
    public String getCollectedBy()              { return collectedBy; }
    public void setCollectedBy(String v)        { this.collectedBy = v; }
    public LocalDate getDistDate()              { return distDate; }
    public void setDistDate(LocalDate v)        { this.distDate = v; }
    public Integer getStockId()                 { return stockId; }
    public void setStockId(Integer v)           { this.stockId = v; }
    public String getRemarks()                  { return remarks; }
    public void setRemarks(String v)            { this.remarks = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
}
