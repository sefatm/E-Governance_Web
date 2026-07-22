package com.mgt.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "distribution_session")
public class DistributionSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "session_code")   private String sessionCode;
    @Column(name = "stock_id")       private int stockId;
    @Column(name = "dealer_name")    private String dealerName;
    @Column(name = "ward")           private String ward;
    @Column(name = "cycle_month")    private String cycleMonth;
    @Column(name = "status")         private String status = "OPEN";
    @Column(name = "opened_at")      private LocalDateTime openedAt;
    @Column(name = "closed_at")      private LocalDateTime closedAt;
    @Column(name = "total_scanned")  private int totalScanned;

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getSessionCode()              { return sessionCode; }
    public void setSessionCode(String v)        { this.sessionCode = v; }
    public int getStockId()                     { return stockId; }
    public void setStockId(int v)               { this.stockId = v; }
    public String getDealerName()               { return dealerName; }
    public void setDealerName(String v)         { this.dealerName = v; }
    public String getWard()                     { return ward; }
    public void setWard(String v)               { this.ward = v; }
    public String getCycleMonth()               { return cycleMonth; }
    public void setCycleMonth(String v)         { this.cycleMonth = v; }
    public String getStatus()                   { return status; }
    public void setStatus(String v)             { this.status = v; }
    public LocalDateTime getOpenedAt()          { return openedAt; }
    public void setOpenedAt(LocalDateTime v)    { this.openedAt = v; }
    public LocalDateTime getClosedAt()          { return closedAt; }
    public void setClosedAt(LocalDateTime v)    { this.closedAt = v; }
    public int getTotalScanned()                { return totalScanned; }
    public void setTotalScanned(int v)          { this.totalScanned = v; }
}
