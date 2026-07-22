package com.mgt.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmer_subsidy_log")
public class FarmerSubsidyLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="card_id",    nullable=false) private int cardId;
    @Column(name="card_no")                   private String cardNo;
    @Column(name="farmer_name")               private String farmerName;
    @Column(name="nid")                       private String nid;
    @Column(name="ward")                      private String ward;
    @Column(name="district")                  private String district;
    @Column(name="subsidy_type",nullable=false)private String subsidyType;
    @Column(name="fertilizer_kg")             private BigDecimal fertilizerKg  = BigDecimal.ZERO;
    @Column(name="seed_kg")                   private BigDecimal seedKg         = BigDecimal.ZERO;
    @Column(name="pesticide_litre")           private BigDecimal pesticideLitre = BigDecimal.ZERO;
    @Column(name="season")                    private String season;
    @Column(name="cycle_month")               private String cycleMonth;
    @Column(name="dist_date")                 private LocalDate distDate;
    @Column(name="distributed_by")            private String distributedBy;
    @Column(name="session_id")                private Integer sessionId;
    @Column(name="remarks",columnDefinition="TEXT") private String remarks;
    @Column(name="created_at")                private LocalDateTime createdAt = LocalDateTime.now();

    public int getId() { return id; }
    public int getCardId() { return cardId; }
    public void setCardId(int v) { this.cardId=v; }
    public String getCardNo() { return cardNo; }
    public void setCardNo(String v) { this.cardNo=v; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String v) { this.farmerName=v; }
    public String getNid() { return nid; }
    public void setNid(String v) { this.nid=v; }
    public String getWard() { return ward; }
    public void setWard(String v) { this.ward=v; }
    public String getDistrict() { return district; }
    public void setDistrict(String v) { this.district=v; }
    public String getSubsidyType() { return subsidyType; }
    public void setSubsidyType(String v) { this.subsidyType=v; }
    public BigDecimal getFertilizerKg() { return fertilizerKg!=null?fertilizerKg:BigDecimal.ZERO; }
    public void setFertilizerKg(BigDecimal v) { this.fertilizerKg=v; }
    public BigDecimal getSeedKg() { return seedKg!=null?seedKg:BigDecimal.ZERO; }
    public void setSeedKg(BigDecimal v) { this.seedKg=v; }
    public BigDecimal getPesticideLitre() { return pesticideLitre!=null?pesticideLitre:BigDecimal.ZERO; }
    public void setPesticideLitre(BigDecimal v) { this.pesticideLitre=v; }
    public String getSeason() { return season; }
    public void setSeason(String v) { this.season=v; }
    public String getCycleMonth() { return cycleMonth; }
    public void setCycleMonth(String v) { this.cycleMonth=v; }
    public LocalDate getDistDate() { return distDate; }
    public void setDistDate(LocalDate v) { this.distDate=v; }
    public String getDistributedBy() { return distributedBy; }
    public void setDistributedBy(String v) { this.distributedBy=v; }
    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer v) { this.sessionId=v; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String v) { this.remarks=v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt=v; }
}
