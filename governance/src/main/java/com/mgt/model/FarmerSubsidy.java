package com.mgt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "farmer_subsidy")
public class FarmerSubsidy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // ✅ FK: farmer_subsidy.card_id → farmer_card.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_subsidy_farmer_card"))
    private FarmerCard card;

    @Column(name = "subsidy_type", length = 100)
    private String subsidyType; // সার / বীজ / কীটনাশক

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit; // কেজি / লিটার

    @Column(name = "dist_date")
    private LocalDate distDate;

    @Column(name = "season", length = 50)
    private String season;

    @Column(name = "distributed_by", length = 100)
    private String distributedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public FarmerCard getCard()                     { return card; }
    public void setCard(FarmerCard card)            { this.card = card; }

    // Convenience method — card_id int দিয়েও set করা যাবে
    public int getCardId()                          { return card != null ? card.getId() : 0; }

    public String getSubsidyType()                  { return subsidyType; }
    public void setSubsidyType(String v)            { this.subsidyType = v; }

    public BigDecimal getQuantity()                 { return quantity; }
    public void setQuantity(BigDecimal v)           { this.quantity = v; }

    public String getUnit()                         { return unit; }
    public void setUnit(String v)                   { this.unit = v; }

    public LocalDate getDistDate()                  { return distDate; }
    public void setDistDate(LocalDate v)            { this.distDate = v; }

    public String getSeason()                       { return season; }
    public void setSeason(String v)                 { this.season = v; }

    public String getDistributedBy()                { return distributedBy; }
    public void setDistributedBy(String v)          { this.distributedBy = v; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime v)       { this.createdAt = v; }
}
