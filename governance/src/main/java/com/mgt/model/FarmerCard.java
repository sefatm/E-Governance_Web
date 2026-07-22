package com.mgt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity(name = "farmerCard")
@Table(name = "farmer_card")
public class FarmerCard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "card_no")
    private String cardNo;

    @Column(name = "farmer_name", nullable = false)
    private String farmerName;

    @Column(name = "nid", nullable = false, unique = true)
    private String nid;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "contact", nullable = false)
    private String contact;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "ward")
    private String ward;

    @Column(name = "union_name")
    private String unionName;

    @Column(name = "upazila")
    private String upazila;

    @Column(name = "district")
    private String district;

    // ── Personal / social ──────────────────────────────────────
    @Column(name = "occupation")
    private String occupation;

    @Column(name = "income_monthly")
    private String incomeMonthly;

    @Column(name = "has_other_card")
    private Boolean hasOtherCard = false;

    // ── Land info ─────────────────────────────────────────────
    @Column(name = "land_own")
    private BigDecimal landOwn = BigDecimal.ZERO;

    @Column(name = "land_lease")
    private BigDecimal landLease = BigDecimal.ZERO;

    @Column(name = "land_total")
    private BigDecimal landTotal = BigDecimal.ZERO;

    @Column(name = "crop_types")
    private String cropTypes;

    @Column(name = "farming_season")
    private String farmingSeason;

    // ── Agriculture detail (new) ───────────────────────────────
    @Column(name = "irrigation_type")
    private String irrigationType;

    @Column(name = "soil_type")
    private String soilType;

    @Column(name = "previous_crop")
    private String previousCrop;

    // ── Financial ─────────────────────────────────────────────
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_branch")
    private String bankBranch;

    // ── Subsidy ───────────────────────────────────────────────
    @Column(name = "fertilizer_quota")
    private BigDecimal fertilizerQuota;

    @Column(name = "seed_quota")
    private BigDecimal seedQuota;

    @Column(name = "last_subsidy_date")
    private LocalDate lastSubsidyDate;

    // ── Land verification (new) ───────────────────────────────
    @Column(name = "land_verified")
    private Boolean landVerified = false;

    @Column(name = "land_verified_by")
    private String landVerifiedBy;

    @Column(name = "land_verified_at")
    private LocalDateTime landVerifiedAt;

    // ── Renewal (new) ─────────────────────────────────────────
    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "renewal_status")
    private String renewalStatus;

    // ── Officer assignment (new) ──────────────────────────────
    @Column(name = "assigned_officer")
    private String assignedOfficer;

    // ── Documents ─────────────────────────────────────────────
    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "nid_file_url")
    private String nidFileUrl;

    @Column(name = "land_doc_url")
    private String landDocUrl;

    // ── Status ────────────────────────────────────────────────
    @Lob
    @Column(name = "certificate_signature", columnDefinition = "LONGTEXT")
    private String certificateSignature;

    @Column(name = "status")
    private String status = "Pending";

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ──────────────────────────────────────

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }
    public String getCardNo()                       { return cardNo; }
    public void setCardNo(String v)                 { this.cardNo = v; }
    public String getFarmerName()                   { return farmerName; }
    public void setFarmerName(String v)             { this.farmerName = v; }
    public String getNid()                          { return nid; }
    public void setNid(String v)                    { this.nid = v; }
    public String getDateOfBirth()                  { return dateOfBirth; }
    public void setDateOfBirth(String v)            { this.dateOfBirth = v; }
    public String getContact()                      { return contact; }
    public void setContact(String v)                { this.contact = v; }
    public String getFatherName()                   { return fatherName; }
    public void setFatherName(String v)             { this.fatherName = v; }
    public String getAddress()                      { return address; }
    public void setAddress(String v)                { this.address = v; }
    public String getWard()                         { return ward; }
    public void setWard(String v)                   { this.ward = v; }
    public String getUnionName()                    { return unionName; }
    public void setUnionName(String v)              { this.unionName = v; }
    public String getUpazila()                      { return upazila; }
    public void setUpazila(String v)                { this.upazila = v; }
    public String getDistrict()                     { return district; }
    public void setDistrict(String v)               { this.district = v; }
    public String getOccupation()                   { return occupation; }
    public void setOccupation(String v)             { this.occupation = v; }
    public String getIncomeMonthly()                { return incomeMonthly; }
    public void setIncomeMonthly(String v)          { this.incomeMonthly = v; }
    public Boolean getHasOtherCard()                { return hasOtherCard; }
    public void setHasOtherCard(Boolean v)          { this.hasOtherCard = v; }
    public BigDecimal getLandOwn()                  { return landOwn != null ? landOwn : BigDecimal.ZERO; }
    public void setLandOwn(BigDecimal v)            { this.landOwn = v; }
    public BigDecimal getLandLease()                { return landLease != null ? landLease : BigDecimal.ZERO; }
    public void setLandLease(BigDecimal v)          { this.landLease = v; }
    public BigDecimal getLandTotal()                { return landTotal != null ? landTotal : BigDecimal.ZERO; }
    public void setLandTotal(BigDecimal v)          { this.landTotal = v; }
    public String getCropTypes()                    { return cropTypes; }
    public void setCropTypes(String v)              { this.cropTypes = v; }
    public String getFarmingSeason()                { return farmingSeason; }
    public void setFarmingSeason(String v)          { this.farmingSeason = v; }
    public String getIrrigationType()               { return irrigationType; }
    public void setIrrigationType(String v)         { this.irrigationType = v; }
    public String getSoilType()                     { return soilType; }
    public void setSoilType(String v)               { this.soilType = v; }
    public String getPreviousCrop()                 { return previousCrop; }
    public void setPreviousCrop(String v)           { this.previousCrop = v; }
    public String getBankName()                     { return bankName; }
    public void setBankName(String v)               { this.bankName = v; }
    public String getBankAccount()                  { return bankAccount; }
    public void setBankAccount(String v)            { this.bankAccount = v; }
    public String getBankBranch()                   { return bankBranch; }
    public void setBankBranch(String v)             { this.bankBranch = v; }
    public BigDecimal getFertilizerQuota()          { return fertilizerQuota; }
    public void setFertilizerQuota(BigDecimal v)    { this.fertilizerQuota = v; }
    public BigDecimal getSeedQuota()                { return seedQuota; }
    public void setSeedQuota(BigDecimal v)          { this.seedQuota = v; }
    public LocalDate getLastSubsidyDate()           { return lastSubsidyDate; }
    public void setLastSubsidyDate(LocalDate v)     { this.lastSubsidyDate = v; }
    public Boolean getLandVerified()                { return landVerified; }
    public void setLandVerified(Boolean v)          { this.landVerified = v; }
    public String getLandVerifiedBy()               { return landVerifiedBy; }
    public void setLandVerifiedBy(String v)         { this.landVerifiedBy = v; }
    public LocalDateTime getLandVerifiedAt()        { return landVerifiedAt; }
    public void setLandVerifiedAt(LocalDateTime v)  { this.landVerifiedAt = v; }
    public LocalDate getExpireDate()                { return expireDate; }
    public void setExpireDate(LocalDate v)          { this.expireDate = v; }
    public String getRenewalStatus()                { return renewalStatus; }
    public void setRenewalStatus(String v)          { this.renewalStatus = v; }
    public String getAssignedOfficer()              { return assignedOfficer; }
    public void setAssignedOfficer(String v)        { this.assignedOfficer = v; }
    public String getPhotoUrl()                     { return photoUrl; }
    public void setPhotoUrl(String v)               { this.photoUrl = v; }
    public String getNidFileUrl()                   { return nidFileUrl; }
    public void setNidFileUrl(String v)             { this.nidFileUrl = v; }
    public String getLandDocUrl()                   { return landDocUrl; }
    public void setLandDocUrl(String v)             { this.landDocUrl = v; }
    public String getStatus()                       { return status; }
    public void setStatus(String v)                 { this.status = v; }
    public String getRejectionReason()              { return rejectionReason; }
    public void setRejectionReason(String v)        { this.rejectionReason = v; }
    public String getApprovedBy()                   { return approvedBy; }
    public void setApprovedBy(String v)             { this.approvedBy = v; }
    public LocalDateTime getApprovedAt()            { return approvedAt; }
    public void setApprovedAt(LocalDateTime v)      { this.approvedAt = v; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime v)       { this.createdAt = v; }
    public String getCertificateSignature() { return certificateSignature; }
    public void setCertificateSignature(String certificateSignature) { this.certificateSignature = certificateSignature; }

}
