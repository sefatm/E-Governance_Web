package com.mgt.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Farmer subsidy stock — সার / বীজ / কীটনাশক মজুদ।
 * TcbStock এর মতো প্যাটার্ন follow করে বানানো।
 *
 * Table: farmer_stock
 */
@Entity
@Table(name = "farmer_stock")
public class FarmerStock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "cycle_month", nullable = false) private String cycleMonth;
    @Column(name = "batch_no")                      private String batchNo;
    @Column(name = "ward")                          private String ward;

    // ── মজুদ পরিমাণ ───────────────────────────────────────────
    @Column(name = "fertilizer_kg",  nullable = false) private BigDecimal fertilizerKg  = BigDecimal.ZERO;
    @Column(name = "seed_kg",        nullable = false) private BigDecimal seedKg         = BigDecimal.ZERO;
    @Column(name = "pesticide_litre")                  private BigDecimal pesticideLitre = BigDecimal.ZERO;

    // ── বিতরণ হওয়া পরিমাণ (distribute() call-এ ঘটে) ─────────
    // TcbStockDAO.deductStock() এর মতো করে UPDATE query-তে কমানো হবে
    @Column(name = "fertilizer_distributed") private BigDecimal fertilizerDistributed = BigDecimal.ZERO;
    @Column(name = "seed_distributed")       private BigDecimal seedDistributed       = BigDecimal.ZERO;

    @Column(name = "note", columnDefinition = "TEXT") private String note;

    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    // ── @PrePersist: NULL guard (TcbStock এর মতো) ─────────────
    @PrePersist
    public void prePersist() {
        if (fertilizerKg          == null) fertilizerKg          = BigDecimal.ZERO;
        if (seedKg                == null) seedKg                = BigDecimal.ZERO;
        if (pesticideLitre        == null) pesticideLitre        = BigDecimal.ZERO;
        if (fertilizerDistributed == null) fertilizerDistributed = BigDecimal.ZERO;
        if (seedDistributed       == null) seedDistributed       = BigDecimal.ZERO;
        if (createdAt             == null) createdAt             = LocalDateTime.now();
        if (updatedAt             == null) updatedAt             = LocalDateTime.now();
    }

    // ── Computed helpers (JPA transient) ──────────────────────
    @Transient
    public BigDecimal getFertilizerRemaining() {
        BigDecimal dist = fertilizerDistributed != null ? fertilizerDistributed : BigDecimal.ZERO;
        BigDecimal stock = fertilizerKg != null ? fertilizerKg : BigDecimal.ZERO;
        return stock.subtract(dist).max(BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getSeedRemaining() {
        BigDecimal dist = seedDistributed != null ? seedDistributed : BigDecimal.ZERO;
        BigDecimal stock = seedKg != null ? seedKg : BigDecimal.ZERO;
        return stock.subtract(dist).max(BigDecimal.ZERO);
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int getId()                               { return id; }
    public String getCycleMonth()                    { return cycleMonth; }
    public void setCycleMonth(String v)              { this.cycleMonth = v; }
    public String getBatchNo()                       { return batchNo; }
    public void setBatchNo(String v)                 { this.batchNo = v; }
    public String getWard()                          { return ward; }
    public void setWard(String v)                    { this.ward = v; }

    public BigDecimal getFertilizerKg()              { return fertilizerKg  != null ? fertilizerKg  : BigDecimal.ZERO; }
    public void setFertilizerKg(BigDecimal v)        { this.fertilizerKg    = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getSeedKg()                    { return seedKg        != null ? seedKg        : BigDecimal.ZERO; }
    public void setSeedKg(BigDecimal v)              { this.seedKg          = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getPesticideLitre()            { return pesticideLitre!= null ? pesticideLitre: BigDecimal.ZERO; }
    public void setPesticideLitre(BigDecimal v)      { this.pesticideLitre  = v != null ? v : BigDecimal.ZERO; }

    public BigDecimal getFertilizerDistributed()     { return fertilizerDistributed != null ? fertilizerDistributed : BigDecimal.ZERO; }
    public void setFertilizerDistributed(BigDecimal v){ this.fertilizerDistributed  = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getSeedDistributed()           { return seedDistributed != null ? seedDistributed : BigDecimal.ZERO; }
    public void setSeedDistributed(BigDecimal v)     { this.seedDistributed   = v != null ? v : BigDecimal.ZERO; }

    public String getNote()                          { return note; }
    public void setNote(String v)                    { this.note = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
}
