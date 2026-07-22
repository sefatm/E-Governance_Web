package com.mgt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "etender_award")
public class ETenderAward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "tender_id", unique = true)
    private int tenderId;

    @Column(name = "bid_id", unique = true)
    private int bidId;

    @Column(name = "awarded_to")
    private String awardedTo;

    @Column(name = "awarded_amount")
    private Double awardedAmount;

    @Column(name = "award_date")
    private LocalDate awardDate;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTenderId() {
		return tenderId;
	}

	public void setTenderId(int tenderId) {
		this.tenderId = tenderId;
	}

	public int getBidId() {
		return bidId;
	}

	public void setBidId(int bidId) {
		this.bidId = bidId;
	}

	public String getAwardedTo() {
		return awardedTo;
	}

	public void setAwardedTo(String awardedTo) {
		this.awardedTo = awardedTo;
	}

	public Double getAwardedAmount() {
		return awardedAmount;
	}

	public void setAwardedAmount(Double awardedAmount) {
		this.awardedAmount = awardedAmount;
	}

	public LocalDate getAwardDate() {
		return awardDate;
	}

	public void setAwardDate(LocalDate awardDate) {
		this.awardDate = awardDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    
}
