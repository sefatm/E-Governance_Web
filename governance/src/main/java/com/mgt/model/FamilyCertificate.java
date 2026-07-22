package com.mgt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity(name = "family")
@Table(name = "family_certificate")
public class FamilyCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "head_name")
    private String headName;

    @Column(name = "nid")
    private String nid;

    @Column(name = "member_count")
    private int memberCount;

    @Column(name = "numbers", columnDefinition = "TEXT")
    private String members;

    @Column(name = "members_json", columnDefinition = "TEXT")
    private String membersJson;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;

    @Column(name = "division")
    private String division;

    @Column(name = "district")
    private String district;

    @Column(name = "contact")
    private String contact;

    @Column(name = "email")
    private String email;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "status")
    private String status = "Pending";

    @Column(name = "certificate_no")
    private String certificateNo;

    @Column(name = "head_photo_url")
    private String headPhotoUrl;

    @Column(name = "head_nid_url")
    private String headNidUrl;

    @Column(name = "member_doc_urls", columnDefinition = "TEXT")
    private String memberDocUrls;


    @Column(name = "approval_stage")
    private Integer approvalStage = 0;

    @Column(name = "first_approved_by")
    private String firstApprovedBy;

    @JsonIgnore
    @Column(name = "first_signature", columnDefinition = "LONGTEXT")
    private String firstSignature;

    @JsonIgnore
    @Column(name = "first_seal", columnDefinition = "LONGTEXT")
    private String firstSeal;

    @Column(name = "first_approved_at")
    private LocalDateTime firstApprovedAt;

    @Column(name = "second_approved_by")
    private String secondApprovedBy;

    @JsonIgnore
    @Column(name = "second_signature", columnDefinition = "LONGTEXT")
    private String secondSignature;

    @JsonIgnore
    @Column(name = "second_seal", columnDefinition = "LONGTEXT")
    private String secondSeal;

    @Column(name = "second_approved_at")
    private LocalDateTime secondApprovedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null)    this.status    = "Pending";
        if (this.approvalStage == null) this.approvalStage = 0;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHeadName() {
		return headName;
	}

	public void setHeadName(String headName) {
		this.headName = headName;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public String getMembers() {
		return members;
	}

	public void setMembers(String members) {
		this.members = members;
	}

	public String getMembersJson() {
		return membersJson;
	}

	public void setMembersJson(String membersJson) {
		this.membersJson = membersJson;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPermanentAddress() {
		return permanentAddress;
	}

	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCertificateNo() {
		return certificateNo;
	}

	public void setCertificateNo(String certificateNo) {
		this.certificateNo = certificateNo;
	}

	public String getHeadPhotoUrl() {
		return headPhotoUrl;
	}

	public void setHeadPhotoUrl(String headPhotoUrl) {
		this.headPhotoUrl = headPhotoUrl;
	}

	public String getHeadNidUrl() {
		return headNidUrl;
	}

	public void setHeadNidUrl(String headNidUrl) {
		this.headNidUrl = headNidUrl;
	}

	public String getMemberDocUrls() {
		return memberDocUrls;
	}

	public void setMemberDocUrls(String memberDocUrls) {
		this.memberDocUrls = memberDocUrls;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


    public Integer getApprovalStage() { return approvalStage; }
    public void setApprovalStage(Integer approvalStage) { this.approvalStage = approvalStage; }
    public String getFirstApprovedBy() { return firstApprovedBy; }
    public void setFirstApprovedBy(String firstApprovedBy) { this.firstApprovedBy = firstApprovedBy; }
    public String getFirstSignature() { return firstSignature; }
    public void setFirstSignature(String firstSignature) { this.firstSignature = firstSignature; }
    public String getFirstSeal() { return firstSeal; }
    public void setFirstSeal(String firstSeal) { this.firstSeal = firstSeal; }
    public LocalDateTime getFirstApprovedAt() { return firstApprovedAt; }
    public void setFirstApprovedAt(LocalDateTime firstApprovedAt) { this.firstApprovedAt = firstApprovedAt; }
    public String getSecondApprovedBy() { return secondApprovedBy; }
    public void setSecondApprovedBy(String secondApprovedBy) { this.secondApprovedBy = secondApprovedBy; }
    public String getSecondSignature() { return secondSignature; }
    public void setSecondSignature(String secondSignature) { this.secondSignature = secondSignature; }
    public String getSecondSeal() { return secondSeal; }
    public void setSecondSeal(String secondSeal) { this.secondSeal = secondSeal; }
    public LocalDateTime getSecondApprovedAt() { return secondApprovedAt; }
    public void setSecondApprovedAt(LocalDateTime secondApprovedAt) { this.secondApprovedAt = secondApprovedAt; }

}
