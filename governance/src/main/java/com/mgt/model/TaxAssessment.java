package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tax_assessment")
public class TaxAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "holdingNo", nullable = false)
    private String holdingNo;

    @Column(name = "ownerName", nullable = false)
    private String ownerName;

    @Column(name = "propertyType")
    private String propertyType;

    @Column(name = "area")
    private Double area;         

    @Column(name = "rate")
    private Double rate;     

    @Column(name = "previousDue")
    private Double previousDue = 0.0;

    @Column(name = "taxAmount")
    private Double taxAmount;    

    @Column(name = "totalPayable")
    private Double totalPayable;   

    @Column(name = "status")
    private String status = "Calculated";

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.taxAmount == null && this.area != null && this.rate != null) {
            this.taxAmount   = this.area * this.rate;
            this.totalPayable = this.taxAmount + (this.previousDue != null ? this.previousDue : 0.0);
        }
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHoldingNo() {
		return holdingNo;
	}

	public void setHoldingNo(String holdingNo) {
		this.holdingNo = holdingNo;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public Double getArea() {
		return area;
	}

	public void setArea(Double area) {
		this.area = area;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Double getPreviousDue() {
		return previousDue;
	}

	public void setPreviousDue(Double previousDue) {
		this.previousDue = previousDue;
	}

	public Double getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(Double taxAmount) {
		this.taxAmount = taxAmount;
	}

	public Double getTotalPayable() {
		return totalPayable;
	}

	public void setTotalPayable(Double totalPayable) {
		this.totalPayable = totalPayable;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    
}
