package com.mgt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * TradeLicenseApply — নতুন fields যোগ করা হয়েছে:
 *
 *   expiryDate      → License কবে expire করবে (approved date + licensePeriod years)
 *   lateFineAmount  → Late renewal এর জন্য fine (টাকা)
 *   lateFineStatus  → "None" / "Pending" / "Paid"
 *   reminder30Sent  → 30 দিন আগের reminder email গেছে কিনা
 *   reminder60Sent  → 60 দিন আগের reminder email গেছে কিনা
 */
@Table(name = "trade_license_apply")
@Entity
public class TradeLicenseApply {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "businessName")
    private String businessName;

    @Column(name = "businessType")
    private String businessType;

    @Column(name = "licensePeriod")
    private Integer licensePeriod;

    @Column(name = "ownername")
    private String ownerName;

    @Column(name = "fatherName")
    private String fatherName;

    @Column(name = "motherName")
    private String motherName;

    @Column(name = "dateOfBirth")
    private String dateOfBirth;

    @Column(name = "nid")
    private String nid;

    @Column(name = "mobile_number")
    private String mobile;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "wardNo")
    private String wardNo;

    @Column(name = "holdingNo")
    private String holdingNo;

    @Column(name = "income")
    private Double income;

    @Column(name = "tax")
    private Double tax;

    @Column(name = "status")
    private String status = "Pending";

    @Column(name = "approval_stage")
    private Integer approvalStage = 0;

    @Column(name = "first_approved_by")
    private String firstApprovedBy;

    @Lob
    @Column(name = "first_signature", columnDefinition = "LONGTEXT")
    private String firstSignature;

    @Column(name = "first_approved_at")
    private LocalDateTime firstApprovedAt;

    @Column(name = "second_approved_by")
    private String secondApprovedBy;

    @Lob
    @Column(name = "second_signature", columnDefinition = "LONGTEXT")
    private String secondSignature;

    @Column(name = "second_approved_at")
    private LocalDateTime secondApprovedAt;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "date")
    private LocalDate appliedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "nid_file_url")
    private String nidFileUrl;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "tax_receipt_file_url")
    private String taxReceiptFileUrl;

    // ── নতুন fields ───────────────────────────────────────────────────────────

    /** License expiry date — Approved হওয়ার পর set হবে */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * Late renewal fine amount (টাকা)
     * Renewal scheduler calculate করবে এবং TradeRenewalService charge করবে
     */
    @Column(name = "late_fine_amount")
    private Double lateFineAmount = 0.0;

    /**
     * Late fine এর payment status
     * "None" = কোনো fine নেই
     * "Pending" = fine আছে, এখনো দেয়নি
     * "Paid" = fine পরিশোধ হয়েছে
     */
    @Column(name = "late_fine_status")
    private String lateFineStatus = "None";

    /** 30-day renewal reminder email পাঠানো হয়েছে কিনা */
    @Column(name = "reminder_30_sent")
    private boolean reminder30Sent = false;

    /** 60-day renewal reminder email পাঠানো হয়েছে কিনা */
    @Column(name = "reminder_60_sent")
    private boolean reminder60Sent = false;

    // ─────────────────────────────────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        if (this.appliedDate    == null) this.appliedDate    = LocalDate.now();
        if (this.createdAt      == null) this.createdAt      = LocalDateTime.now();
        if (this.status         == null) this.status         = "Pending";
        if (this.approvalStage  == null) this.approvalStage  = 0;
        if (this.lateFineAmount == null) this.lateFineAmount = 0.0;
        if (this.lateFineStatus == null) this.lateFineStatus = "None";
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public Integer getLicensePeriod() { return licensePeriod; }
    public void setLicensePeriod(Integer licensePeriod) { this.licensePeriod = licensePeriod; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWardNo() { return wardNo; }
    public void setWardNo(String wardNo) { this.wardNo = wardNo; }

    public String getHoldingNo() { return holdingNo; }
    public void setHoldingNo(String holdingNo) { this.holdingNo = holdingNo; }

    public Double getIncome() { return income; }
    public void setIncome(Double income) { this.income = income; }

    public Double getTax() { return tax; }
    public void setTax(Double tax) { this.tax = tax; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNidFileUrl() { return nidFileUrl; }
    public void setNidFileUrl(String nidFileUrl) { this.nidFileUrl = nidFileUrl; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getTaxReceiptFileUrl() { return taxReceiptFileUrl; }
    public void setTaxReceiptFileUrl(String taxReceiptFileUrl) { this.taxReceiptFileUrl = taxReceiptFileUrl; }

    public Integer getApprovalStage() { return approvalStage; }
    public void setApprovalStage(Integer v) { this.approvalStage = v; }
    public String getFirstApprovedBy() { return firstApprovedBy; }
    public void setFirstApprovedBy(String v) { this.firstApprovedBy = v; }
    public String getFirstSignature() { return firstSignature; }
    public void setFirstSignature(String v) { this.firstSignature = v; }
    public LocalDateTime getFirstApprovedAt() { return firstApprovedAt; }
    public void setFirstApprovedAt(LocalDateTime v) { this.firstApprovedAt = v; }
    public String getSecondApprovedBy() { return secondApprovedBy; }
    public void setSecondApprovedBy(String v) { this.secondApprovedBy = v; }
    public String getSecondSignature() { return secondSignature; }
    public void setSecondSignature(String v) { this.secondSignature = v; }
    public LocalDateTime getSecondApprovedAt() { return secondApprovedAt; }
    public void setSecondApprovedAt(LocalDateTime v) { this.secondApprovedAt = v; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Double getLateFineAmount() { return lateFineAmount; }
    public void setLateFineAmount(Double lateFineAmount) { this.lateFineAmount = lateFineAmount; }

    public String getLateFineStatus() { return lateFineStatus; }
    public void setLateFineStatus(String lateFineStatus) { this.lateFineStatus = lateFineStatus; }

    public boolean isReminder30Sent() { 
    	return reminder30Sent; }
    
    public void setReminder30Sent(boolean reminder30Sent) { 
    	this.reminder30Sent = reminder30Sent; }

    public boolean isReminder60Sent() { 
    	return reminder60Sent; }
    
    public void setReminder60Sent(boolean reminder60Sent) { 
    	this.reminder60Sent = reminder60Sent; }
}
