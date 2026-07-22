package com.mgt.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tcb_stock")
public class TcbStock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "batch_label")    private String batchLabel;
    @Column(name = "cycle_month")    private String cycleMonth;
    @Column(name = "ward")           private String ward;
    @Column(name = "dealer_name")    private String dealerName;

    @Column(name = "oil_litre")      private BigDecimal oilLitre   = BigDecimal.ZERO;
    @Column(name = "rice_kg")        private BigDecimal riceKg     = BigDecimal.ZERO;
    @Column(name = "lentil_kg")      private BigDecimal lentilKg   = BigDecimal.ZERO;
    @Column(name = "sugar_kg")       private BigDecimal sugarKg    = BigDecimal.ZERO;
    @Column(name = "cash_amount")    private BigDecimal cashAmount = BigDecimal.ZERO;

    // ✅ NEW: প্রতিটি পণ্যের একক মূল্য (টাকায়)
    @Column(name = "oil_price_per_litre")    private BigDecimal oilPricePerLitre   = BigDecimal.ZERO;
    @Column(name = "rice_price_per_kg")      private BigDecimal ricePricePerKg     = BigDecimal.ZERO;
    @Column(name = "lentil_price_per_kg")    private BigDecimal lentilPricePerKg   = BigDecimal.ZERO;
    @Column(name = "sugar_price_per_kg")     private BigDecimal sugarPricePerKg    = BigDecimal.ZERO;

    @Column(name = "total_cards")    private int totalCards;

    // ✅ FIX: DB column is DEFAULT NULL — use Integer (wrapper) to avoid NPE
    // when Hibernate maps a NULL int column to primitive int, it throws NullPointerException
    @Column(name = "distributed")    private Integer distributed = 0;

    @Column(name = "created_at")     private LocalDateTime createdAt;
    @Column(name = "updated_at")     private LocalDateTime updatedAt;

    // ✅ FIX: @PrePersist ensures distributed is never NULL on insert
    @PrePersist
    public void prePersist() {
        if (this.distributed == null) this.distributed = 0;
        if (this.createdAt   == null) this.createdAt   = LocalDateTime.now();
        if (this.updatedAt   == null) this.updatedAt   = LocalDateTime.now();
        if (this.oilLitre    == null) this.oilLitre    = BigDecimal.ZERO;
        if (this.riceKg      == null) this.riceKg      = BigDecimal.ZERO;
        if (this.lentilKg    == null) this.lentilKg    = BigDecimal.ZERO;
        if (this.sugarKg     == null) this.sugarKg     = BigDecimal.ZERO;
        if (this.cashAmount  == null) this.cashAmount  = BigDecimal.ZERO;
        if (this.oilPricePerLitre  == null) this.oilPricePerLitre  = BigDecimal.ZERO;
        if (this.ricePricePerKg    == null) this.ricePricePerKg    = BigDecimal.ZERO;
        if (this.lentilPricePerKg  == null) this.lentilPricePerKg  = BigDecimal.ZERO;
        if (this.sugarPricePerKg   == null) this.sugarPricePerKg   = BigDecimal.ZERO;
    }

    // getters & setters
    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getBatchLabel()               { return batchLabel; }
    public void setBatchLabel(String v)         { this.batchLabel = v; }
    public String getCycleMonth()               { return cycleMonth; }
    public void setCycleMonth(String v)         { this.cycleMonth = v; }
    public String getWard()                     { return ward; }
    public void setWard(String v)               { this.ward = v; }
    public String getDealerName()               { return dealerName; }
    public void setDealerName(String v)         { this.dealerName = v; }
    public BigDecimal getOilLitre()             { return oilLitre  != null ? oilLitre  : BigDecimal.ZERO; }
    public void setOilLitre(BigDecimal v)       { this.oilLitre    = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getRiceKg()               { return riceKg    != null ? riceKg    : BigDecimal.ZERO; }
    public void setRiceKg(BigDecimal v)         { this.riceKg      = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getLentilKg()             { return lentilKg  != null ? lentilKg  : BigDecimal.ZERO; }
    public void setLentilKg(BigDecimal v)       { this.lentilKg    = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getSugarKg()              { return sugarKg   != null ? sugarKg   : BigDecimal.ZERO; }
    public void setSugarKg(BigDecimal v)        { this.sugarKg     = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getCashAmount()           { return cashAmount != null ? cashAmount : BigDecimal.ZERO; }

    // ✅ NEW: price getters & setters
    public BigDecimal getOilPricePerLitre()     { return oilPricePerLitre  != null ? oilPricePerLitre  : BigDecimal.ZERO; }
    public void setOilPricePerLitre(BigDecimal v)  { this.oilPricePerLitre  = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getRicePricePerKg()       { return ricePricePerKg    != null ? ricePricePerKg    : BigDecimal.ZERO; }
    public void setRicePricePerKg(BigDecimal v)    { this.ricePricePerKg    = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getLentilPricePerKg()     { return lentilPricePerKg  != null ? lentilPricePerKg  : BigDecimal.ZERO; }
    public void setLentilPricePerKg(BigDecimal v)  { this.lentilPricePerKg  = v != null ? v : BigDecimal.ZERO; }
    public BigDecimal getSugarPricePerKg()      { return sugarPricePerKg   != null ? sugarPricePerKg   : BigDecimal.ZERO; }
    public void setSugarPricePerKg(BigDecimal v)   { this.sugarPricePerKg   = v != null ? v : BigDecimal.ZERO; }
    public void setCashAmount(BigDecimal v)     { this.cashAmount  = v != null ? v : BigDecimal.ZERO; }
    public int getTotalCards()                  { return totalCards; }
    public void setTotalCards(int v)            { this.totalCards  = v; }

    // ✅ FIX: safe getter — never returns null, never throws NPE
    public int getDistributed()                 { return distributed != null ? distributed : 0; }
    public void setDistributed(Integer v)       { this.distributed = v != null ? v : 0; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }
}
