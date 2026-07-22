package com.mgt.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * TradeRenewal — নতুন fields:
 *   lateFineAmount  → কত টাকা late fine চার্জ হয়েছে
 *   lateFineStatus  → "None" / "Pending" / "Paid"
 */
@Table(name = "trade_license_renewal")
@Entity
public class TradeRenewal {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String applicantName;
    private String fatherName;
    private String motherName;
    private String dateOfBirth;
    private String businessName;
    private String businessType;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "license_id")
    private TradeLicenseApply originalLicense;

    private String licenseExpiry;
    private String issuingAuthority;
    private String nid;
    private String address;

    @Column(name = "ward_no")
    private String wardNo;

    @Column(name = "holding_no")
    private String holdingNo;

    private String contact;
    private String email;
    private int    renewalPeriod;
    private double annualIncome;

    @Column(name = "tax_paid")
    private double taxPaid;

    private String  purpose;
    private boolean declaration;
    private String  status = "Pending";
    private LocalDateTime createdAt;

    @Column(name = "nid_file_url")
    private String nidFileUrl;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "license_file_url")
    private String licenseFileUrl;

    // ── নতুন: Late Fine fields ────────────────────────────────────────────────

    /** Late renewal fine amount — TradeRenewalService calculate করে set করে */
    @Column(name = "late_fine_amount")
    private double lateFineAmount = 0.0;

    /**
     * Late fine payment status
     * "None"    → কোনো fine নেই (সময়মতো renew করেছে)
     * "Pending" → fine আছে, এখনো দেয়নি
     * "Paid"    → fine পরিশোধ হয়েছে
     */
    @Column(name = "late_fine_status")
    private String lateFineStatus = "None";

    // Two-step renewal approval: signature only (no seal)
    @Column(name = "approval_stage")
    private Integer approvalStage = 0;

    @Column(name = "first_approved_by")
    private String firstApprovedBy;

    @Column(name = "first_approved_at")
    private LocalDateTime firstApprovedAt;

    @Lob
    @Column(name = "first_signature", columnDefinition = "LONGTEXT")
    private String firstSignature;

    @Column(name = "second_approved_by")
    private String secondApprovedBy;

    @Column(name = "second_approved_at")
    private LocalDateTime secondApprovedAt;

    @Lob
    @Column(name = "second_signature", columnDefinition = "LONGTEXT")
    private String secondSignature;

    // ─────────────────────────────────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status        == null) this.status        = "Pending";
        if (this.lateFineStatus == null) this.lateFineStatus = "None";
        if (this.approvalStage == null) this.approvalStage = 0;
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public TradeLicenseApply getOriginalLicense() { return originalLicense; }
    public void setOriginalLicense(TradeLicenseApply originalLicense) { this.originalLicense = originalLicense; }

    public String getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(String licenseExpiry) { this.licenseExpiry = licenseExpiry; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWardNo() { return wardNo; }
    public void setWardNo(String wardNo) { this.wardNo = wardNo; }

    public String getHoldingNo() { return holdingNo; }
    public void setHoldingNo(String holdingNo) { this.holdingNo = holdingNo; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getRenewalPeriod() { return renewalPeriod; }
    public void setRenewalPeriod(int renewalPeriod) { this.renewalPeriod = renewalPeriod; }

    public double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(double annualIncome) { this.annualIncome = annualIncome; }

    public double getTaxPaid() { return taxPaid; }
    public void setTaxPaid(double taxPaid) { this.taxPaid = taxPaid; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public boolean isDeclaration() { return declaration; }
    public void setDeclaration(boolean declaration) { this.declaration = declaration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNidFileUrl() { return nidFileUrl; }
    public void setNidFileUrl(String nidFileUrl) { this.nidFileUrl = nidFileUrl; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getLicenseFileUrl() { return licenseFileUrl; }
    public void setLicenseFileUrl(String licenseFileUrl) { this.licenseFileUrl = licenseFileUrl; }

    public double getLateFineAmount() { return lateFineAmount; }
    public void setLateFineAmount(double lateFineAmount) { this.lateFineAmount = lateFineAmount; }

    public String getLateFineStatus() { return lateFineStatus; }
    public void setLateFineStatus(String lateFineStatus) { this.lateFineStatus = lateFineStatus; }

    public Integer getApprovalStage() { return approvalStage; }
    public void setApprovalStage(Integer approvalStage) { this.approvalStage = approvalStage; }

    public String getFirstApprovedBy() { return firstApprovedBy; }
    public void setFirstApprovedBy(String firstApprovedBy) { this.firstApprovedBy = firstApprovedBy; }

    public LocalDateTime getFirstApprovedAt() { return firstApprovedAt; }
    public void setFirstApprovedAt(LocalDateTime firstApprovedAt) { this.firstApprovedAt = firstApprovedAt; }

    public String getFirstSignature() { return firstSignature; }
    public void setFirstSignature(String firstSignature) { this.firstSignature = firstSignature; }

    public String getSecondApprovedBy() { return secondApprovedBy; }
    public void setSecondApprovedBy(String secondApprovedBy) { this.secondApprovedBy = secondApprovedBy; }

    public LocalDateTime getSecondApprovedAt() { return secondApprovedAt; }
    public void setSecondApprovedAt(LocalDateTime secondApprovedAt) { this.secondApprovedAt = secondApprovedAt; }

    public String getSecondSignature() { return secondSignature; }
    public void setSecondSignature(String secondSignature) { this.secondSignature = secondSignature; }
}

