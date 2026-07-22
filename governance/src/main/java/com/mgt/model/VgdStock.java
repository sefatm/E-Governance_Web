package com.mgt.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="vgd_stock")
public class VgdStock {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private int id;
    @Column(name="batch_label",nullable=false) private String batchLabel;
    @Column(name="cycle_month",nullable=false) private String cycleMonth;
    @Column(name="card_type",  nullable=false) private String cardType="VGD";
    @Column(name="ward")                       private String ward;
    @Column(name="district")                   private String district;
    @Column(name="dealer_name")                private String dealerName;
    @Column(name="rice_kg")                    private BigDecimal riceKg   =BigDecimal.ZERO;
    @Column(name="wheat_kg")                   private BigDecimal wheatKg  =BigDecimal.ZERO;
    @Column(name="cash_amount")                private BigDecimal cashAmount=BigDecimal.ZERO;
    @Column(name="total_cards",nullable=false) private int totalCards;
    @Column(name="distributed",nullable=false) private int distributed=0;
    @Column(name="created_at")                 private LocalDateTime createdAt=LocalDateTime.now();
    @Column(name="updated_at")                 private LocalDateTime updatedAt=LocalDateTime.now();
    @PrePersist public void pre(){ if(riceKg==null)riceKg=BigDecimal.ZERO;if(wheatKg==null)wheatKg=BigDecimal.ZERO;if(cashAmount==null)cashAmount=BigDecimal.ZERO; }
    @PreUpdate  public void upd(){ updatedAt=LocalDateTime.now(); }
    public int getId(){return id;}
    public String getBatchLabel(){return batchLabel;} public void setBatchLabel(String v){batchLabel=v;}
    public String getCycleMonth(){return cycleMonth;} public void setCycleMonth(String v){cycleMonth=v;}
    public String getCardType()  {return cardType;}   public void setCardType(String v)  {cardType=v;}
    public String getWard()      {return ward;}        public void setWard(String v)      {ward=v;}
    public String getDistrict()  {return district;}    public void setDistrict(String v)  {district=v;}
    public String getDealerName(){return dealerName;}  public void setDealerName(String v){dealerName=v;}
    public BigDecimal getRiceKg()    {return riceKg!=null?riceKg:BigDecimal.ZERO;}    public void setRiceKg(BigDecimal v)    {riceKg=v;}
    public BigDecimal getWheatKg()   {return wheatKg!=null?wheatKg:BigDecimal.ZERO;}  public void setWheatKg(BigDecimal v)   {wheatKg=v;}
    public BigDecimal getCashAmount(){return cashAmount!=null?cashAmount:BigDecimal.ZERO;} public void setCashAmount(BigDecimal v){cashAmount=v;}
    public int getTotalCards() {return totalCards;}  public void setTotalCards(int v) {totalCards=v;}
    public int getDistributed(){return distributed;} public void setDistributed(int v){distributed=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
