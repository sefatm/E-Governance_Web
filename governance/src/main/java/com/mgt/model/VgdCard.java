package com.mgt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity(name = "vgdCard")
@Table(name = "vgd_card")
public class VgdCard {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "card_no")
    private String cardNo;

    @Column(name = "card_type", nullable = false)
    private String cardType; 

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(name = "nid", nullable = false, unique = true)
    private String nid;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "contact")
    private String contact;

    @Column(name = "husband_name")
    private String husbandName;

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

    // Vulnerability
    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "disability")
    private String disability;

    @Column(name = "has_land")
    private boolean hasLand = false;
    
    @Column(name = "has_other_card")
    private boolean hasOtherCard;

	@Column(name = "land_area")
    private BigDecimal landArea = BigDecimal.ZERO;

    @Column(name = "income_monthly")
    private String incomeMonthly;

    @Column(name = "members_count")
    private int membersCount = 1;

    @Column(name = "children_count")
    private int childrenCount = 0;

    @Column(name = "monthly_rice_kg")
    private BigDecimal monthlyRiceKg = new BigDecimal("30.00");

    @Column(name = "monthly_wheat_kg")
    private BigDecimal monthlyWheatKg = BigDecimal.ZERO;

    @Column(name = "cash_amount")
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(name = "cycle_months")
    private int cycleMonths = 24;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "last_received_date")
    private LocalDate lastReceivedDate;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "mobile_banking")
    private String mobileBanking;

    @Column(name = "mobile_banking_no")
    private String mobileBankingNo;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "nid_file_url")
    private String nidFileUrl;

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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getHusbandName() {
		return husbandName;
	}

	public void setHusbandName(String husbandName) {
		this.husbandName = husbandName;
	}

	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getWard() {
		return ward;
	}

	public void setWard(String ward) {
		this.ward = ward;
	}

	public String getUnionName() {
		return unionName;
	}

	public void setUnionName(String unionName) {
		this.unionName = unionName;
	}

	public String getUpazila() {
		return upazila;
	}

	public void setUpazila(String upazila) {
		this.upazila = upazila;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public String getDisability() {
		return disability;
	}

	public void setDisability(String disability) {
		this.disability = disability;
	}

	public boolean isHasLand() {
		return hasLand;
	}

	public void setHasLand(boolean hasLand) {
		this.hasLand = hasLand;
	}

	public BigDecimal getLandArea() {
		return landArea;
	}

	public void setLandArea(BigDecimal landArea) {
		this.landArea = landArea;
	}

	public String getIncomeMonthly() {
		return incomeMonthly;
	}

	public void setIncomeMonthly(String incomeMonthly) {
		this.incomeMonthly = incomeMonthly;
	}

	public int getMembersCount() {
		return membersCount;
	}

	public void setMembersCount(int membersCount) {
		this.membersCount = membersCount;
	}

	public int getChildrenCount() {
		return childrenCount;
	}

	public void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}

	public BigDecimal getMonthlyRiceKg() {
		return monthlyRiceKg;
	}

	public void setMonthlyRiceKg(BigDecimal monthlyRiceKg) {
		this.monthlyRiceKg = monthlyRiceKg;
	}

	public BigDecimal getMonthlyWheatKg() {
		return monthlyWheatKg;
	}

	public void setMonthlyWheatKg(BigDecimal monthlyWheatKg) {
		this.monthlyWheatKg = monthlyWheatKg;
	}

	public BigDecimal getCashAmount() {
		return cashAmount;
	}

	public void setCashAmount(BigDecimal cashAmount) {
		this.cashAmount = cashAmount;
	}

	public int getCycleMonths() {
		return cycleMonths;
	}

	public void setCycleMonths(int cycleMonths) {
		this.cycleMonths = cycleMonths;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public LocalDate getLastReceivedDate() {
		return lastReceivedDate;
	}

	public void setLastReceivedDate(LocalDate lastReceivedDate) {
		this.lastReceivedDate = lastReceivedDate;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}

	public String getMobileBanking() {
		return mobileBanking;
	}

	public void setMobileBanking(String mobileBanking) {
		this.mobileBanking = mobileBanking;
	}

	public String getMobileBankingNo() {
		return mobileBankingNo;
	}

	public void setMobileBankingNo(String mobileBankingNo) {
		this.mobileBankingNo = mobileBankingNo;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getNidFileUrl() {
		return nidFileUrl;
	}

	public void setNidFileUrl(String nidFileUrl) {
		this.nidFileUrl = nidFileUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(LocalDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    public boolean isHasOtherCard() {
		return hasOtherCard;
	}

	public void setHasOtherCard(boolean hasOtherCard) {
		this.hasOtherCard = hasOtherCard;
	}
    
    public String getCertificateSignature() { return certificateSignature; }
    public void setCertificateSignature(String certificateSignature) { this.certificateSignature = certificateSignature; }

}
