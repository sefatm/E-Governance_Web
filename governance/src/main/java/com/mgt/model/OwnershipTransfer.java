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
@Table(name = "ownership_transfer")
public class OwnershipTransfer {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "currentOwner")
    private String currentOwner;

    @Column(name = "currentOwnerNid")
    private String currentOwnerNid;

    @Column(name = "newOwner")
    private String newOwner;

    @Column(name = "newOwnerNid")
    private String newOwnerNid;

    @Column(name = "contact")
    private String contact;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "holdingNumber")
    private String holdingNumber;

    @Column(name = "wardNo")
    private String wardNo;

    @Column(name = "address")
    private String address;

    @Column(name = "reason")
    private String reason;

    @Column(name = "currentOwnerNidFileUrl")
    private String currentOwnerNidFileUrl;

    @Column(name = "newOwnerNidFileUrl")
    private String newOwnerNidFileUrl;

    @Column(name = "deedFileUrl")
    private String deedFileUrl;

    @Column(name = "status")
    private String status = "Pending";

    @Column(name = "rejectReason")
    private String rejectReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "approval_stage")
    private Integer approvalStage = 0;

    @Column(name = "first_approved_by")
    private String firstApprovedBy;

    @Column(name = "first_approved_at")
    private LocalDateTime firstApprovedAt;

    @Column(name = "first_signature", columnDefinition = "LONGTEXT")
    private String firstSignature;

    @Column(name = "first_seal", columnDefinition = "LONGTEXT")
    private String firstSeal;

    @Column(name = "second_approved_by")
    private String secondApprovedBy;

    @Column(name = "second_approved_at")
    private LocalDateTime secondApprovedAt;

    @Column(name = "second_signature", columnDefinition = "LONGTEXT")
    private String secondSignature;

    @Column(name = "second_seal", columnDefinition = "LONGTEXT")
    private String secondSeal;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "Pending";
        if (this.approvalStage == null) this.approvalStage = 0;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCurrentOwner() {
		return currentOwner;
	}

	public void setCurrentOwner(String currentOwner) {
		this.currentOwner = currentOwner;
	}

	public String getCurrentOwnerNid() {
		return currentOwnerNid;
	}

	public void setCurrentOwnerNid(String currentOwnerNid) {
		this.currentOwnerNid = currentOwnerNid;
	}

	public String getNewOwner() {
		return newOwner;
	}

	public void setNewOwner(String newOwner) {
		this.newOwner = newOwner;
	}

	public String getNewOwnerNid() {
		return newOwnerNid;
	}

	public void setNewOwnerNid(String newOwnerNid) {
		this.newOwnerNid = newOwnerNid;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getHoldingNumber() {
		return holdingNumber;
	}

	public void setHoldingNumber(String holdingNumber) {
		this.holdingNumber = holdingNumber;
	}

	public String getWardNo() {
		return wardNo;
	}

	public void setWardNo(String wardNo) {
		this.wardNo = wardNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getCurrentOwnerNidFileUrl() {
		return currentOwnerNidFileUrl;
	}

	public void setCurrentOwnerNidFileUrl(String currentOwnerNidFileUrl) {
		this.currentOwnerNidFileUrl = currentOwnerNidFileUrl;
	}

	public String getNewOwnerNidFileUrl() {
		return newOwnerNidFileUrl;
	}

	public void setNewOwnerNidFileUrl(String newOwnerNidFileUrl) {
		this.newOwnerNidFileUrl = newOwnerNidFileUrl;
	}

	public String getDeedFileUrl() {
		return deedFileUrl;
	}

	public void setDeedFileUrl(String deedFileUrl) {
		this.deedFileUrl = deedFileUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRejectReason() {
		return rejectReason;
	}

	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    public Integer getApprovalStage() { return approvalStage; }
    public void setApprovalStage(Integer approvalStage) { this.approvalStage = approvalStage; }

    public String getFirstApprovedBy() { return firstApprovedBy; }
    public void setFirstApprovedBy(String firstApprovedBy) { this.firstApprovedBy = firstApprovedBy; }

    public LocalDateTime getFirstApprovedAt() { return firstApprovedAt; }
    public void setFirstApprovedAt(LocalDateTime firstApprovedAt) { this.firstApprovedAt = firstApprovedAt; }

    public String getFirstSignature() { return firstSignature; }
    public void setFirstSignature(String firstSignature) { this.firstSignature = firstSignature; }

    public String getFirstSeal() { return firstSeal; }
    public void setFirstSeal(String firstSeal) { this.firstSeal = firstSeal; }

    public String getSecondApprovedBy() { return secondApprovedBy; }
    public void setSecondApprovedBy(String secondApprovedBy) { this.secondApprovedBy = secondApprovedBy; }

    public LocalDateTime getSecondApprovedAt() { return secondApprovedAt; }
    public void setSecondApprovedAt(LocalDateTime secondApprovedAt) { this.secondApprovedAt = secondApprovedAt; }

    public String getSecondSignature() { return secondSignature; }
    public void setSecondSignature(String secondSignature) { this.secondSignature = secondSignature; }

    public String getSecondSeal() { return secondSeal; }
    public void setSecondSeal(String secondSeal) { this.secondSeal = secondSeal; }
}

