package com.mgt.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "distribution_log")
public class DistributionLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "session_id")     private int sessionId;
    @Column(name = "card_no")        private String cardNo;
    @Column(name = "card_type")      private String cardType;
    @Column(name = "holder_name")    private String holderName;
    @Column(name = "nid")            private String nid;
    @Column(name = "ward")           private String ward;
    @Column(name = "oil_litre")      private BigDecimal oilLitre   = BigDecimal.ZERO;
    @Column(name = "rice_kg")        private BigDecimal riceKg     = BigDecimal.ZERO;
    @Column(name = "lentil_kg")      private BigDecimal lentilKg   = BigDecimal.ZERO;
    @Column(name = "sugar_kg")       private BigDecimal sugarKg    = BigDecimal.ZERO;
    @Column(name = "cash_amount")    private BigDecimal cashAmount = BigDecimal.ZERO;
    @Column(name = "scanned_at")     private LocalDateTime scannedAt;
    @Column(name = "scanned_by")     private String scannedBy;

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public int getSessionId()                   { return sessionId; }
    public void setSessionId(int v)             { this.sessionId = v; }
    public String getCardNo()                   { return cardNo; }
    public void setCardNo(String v)             { this.cardNo = v; }
    public String getCardType()                 { return cardType; }
    public void setCardType(String v)           { this.cardType = v; }
    public String getHolderName()               { return holderName; }
    public void setHolderName(String v)         { this.holderName = v; }
    public String getNid()                      { return nid; }
    public void setNid(String v)                { this.nid = v; }
    public String getWard()                     { return ward; }
    public void setWard(String v)               { this.ward = v; }
    public BigDecimal getOilLitre()             { return oilLitre; }
    public void setOilLitre(BigDecimal v)       { this.oilLitre = v; }
    public BigDecimal getRiceKg()               { return riceKg; }
    public void setRiceKg(BigDecimal v)         { this.riceKg = v; }
    public BigDecimal getLentilKg()             { return lentilKg; }
    public void setLentilKg(BigDecimal v)       { this.lentilKg = v; }
    public BigDecimal getSugarKg()              { return sugarKg; }
    public void setSugarKg(BigDecimal v)        { this.sugarKg = v; }
    public BigDecimal getCashAmount()           { return cashAmount; }
    public void setCashAmount(BigDecimal v)     { this.cashAmount = v; }
    public LocalDateTime getScannedAt()         { return scannedAt; }
    public void setScannedAt(LocalDateTime v)   { this.scannedAt = v; }
    public String getScannedBy()                { return scannedBy; }
    public void setScannedBy(String v)          { this.scannedBy = v; }
}
