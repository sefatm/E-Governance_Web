package com.mgt.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="vgd_session")
public class VgdSession {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private int id;
    @Column(name="session_code", unique=true,nullable=false) private String sessionCode;
    @Column(name="stock_id")                                 private Integer stockId;
    @Column(name="card_type",    nullable=false)             private String cardType="VGD";
    @Column(name="cycle_month",  nullable=false)             private String cycleMonth;
    @Column(name="ward")                                     private String ward;
    @Column(name="dealer_name")                              private String dealerName;
    @Column(name="status")                                   private String status="OPEN";
    @Column(name="total_scanned",nullable=false)             private int totalScanned=0;
    @Column(name="opened_at")                                private LocalDateTime openedAt=LocalDateTime.now();
    @Column(name="closed_at")                                private LocalDateTime closedAt;
    public int getId(){return id;}
    public String getSessionCode(){return sessionCode;} public void setSessionCode(String v){sessionCode=v;}
    public Integer getStockId()   {return stockId;}     public void setStockId(Integer v)   {stockId=v;}
    public String getCardType()   {return cardType;}    public void setCardType(String v)   {cardType=v;}
    public String getCycleMonth() {return cycleMonth;}  public void setCycleMonth(String v) {cycleMonth=v;}
    public String getWard()       {return ward;}         public void setWard(String v)       {ward=v;}
    public String getDealerName() {return dealerName;}  public void setDealerName(String v) {dealerName=v;}
    public String getStatus()     {return status;}      public void setStatus(String v)     {status=v;}
    public int getTotalScanned()  {return totalScanned;}public void setTotalScanned(int v)  {totalScanned=v;}
    public LocalDateTime getOpenedAt(){return openedAt;} public void setOpenedAt(LocalDateTime v){openedAt=v;}
    public LocalDateTime getClosedAt(){return closedAt;} public void setClosedAt(LocalDateTime v){closedAt=v;}
}
