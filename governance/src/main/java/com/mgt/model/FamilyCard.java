package com.mgt.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity(name = "familyCard")
@Table(name = "family_card")
public class FamilyCard {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "card_no")
    private String cardNo;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(name = "nid", nullable = false, unique = true)
    private String nid;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "contact", nullable = false)
    private String contact;

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

    @Column(name = "members_count")
    private int membersCount = 1;

    @Column(name = "income_monthly")
    private String incomeMonthly;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "husband_or_father_name")
    private String husbandOrFatherName;

    @Column(name = "has_other_card")
    private boolean hasOtherCard = false;

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
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status    == null) this.status    = "Pending";
    }

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

	public int getMembersCount() {
		return membersCount;
	}

	public void setMembersCount(int membersCount) {
		this.membersCount = membersCount;
	}

	public String getIncomeMonthly() {
		return incomeMonthly;
	}

	public void setIncomeMonthly(String incomeMonthly) {
		this.incomeMonthly = incomeMonthly;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public String getHusbandOrFatherName() {
		return husbandOrFatherName;
	}

	public void setHusbandOrFatherName(String husbandOrFatherName) {
		this.husbandOrFatherName = husbandOrFatherName;
	}

	public boolean isHasOtherCard() {
		return hasOtherCard;
	}

	public void setHasOtherCard(boolean hasOtherCard) {
		this.hasOtherCard = hasOtherCard;
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

    
    public String getCertificateSignature() { return certificateSignature; }
    public void setCertificateSignature(String certificateSignature) { this.certificateSignature = certificateSignature; }

}
