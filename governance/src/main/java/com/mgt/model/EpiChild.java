package com.mgt.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "epi_child")
public class EpiChild {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "child_name",    nullable = false)
    private String childName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender",        nullable = false)
    private String gender;

    @Column(name = "father_name",   nullable = false)
    private String fatherName;

    @Column(name = "mother_name",   nullable = false)
    private String motherName;

    @Column(name = "guardian_nid")
    private String guardianNid;

    @Column(name = "father_nid")
    private String fatherNid;

    @Column(name = "mother_nid")
    private String motherNid;

    @Column(name = "guardian_phone", nullable = false)
    private String guardianPhone;

    @Column(name = "guardian_email")
    private String guardianEmail;

    @Column(name = "ward")
    private String ward;

    @Column(name = "union_name")
    private String unionName;

    @Column(name = "upazila")
    private String upazila;

    @Column(name = "district")
    private String district;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "present_address", columnDefinition = "TEXT")
    private String presentAddress;

    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;

    @Column(name = "birth_place")
    private String birthPlace;

    @Column(name = "child_photo_url")
    private String childPhotoUrl;

    @Column(name = "father_nid_file_url")
    private String fatherNidFileUrl;

    @Column(name = "mother_nid_file_url")
    private String motherNidFileUrl;

    @Column(name = "card_no", unique = true)
    private String cardNo;

    @Column(name = "status")
    private String status; // Pending | Approved

    @Column(name = "registered_by")
    private String registeredBy;

    @Column(name = "approval_stage")
    private Integer approvalStage = 0;

    @Column(name = "first_approved_by")
    private String firstApprovedBy;

    @Lob
    @Column(name = "first_signature", columnDefinition = "LONGTEXT")
    private String firstSignature;

    @Lob
    @Column(name = "first_seal", columnDefinition = "LONGTEXT")
    private String firstSeal;

    @Column(name = "first_approved_at")
    private LocalDateTime firstApprovedAt;

    @Column(name = "second_approved_by")
    private String secondApprovedBy;

    @Lob
    @Column(name = "second_signature", columnDefinition = "LONGTEXT")
    private String secondSignature;

    @Lob
    @Column(name = "second_seal", columnDefinition = "LONGTEXT")
    private String secondSeal;

    @Column(name = "second_approved_at")
    private LocalDateTime secondApprovedAt;

    @Lob
    @Column(name = "authority_signature", columnDefinition = "LONGTEXT")
    private String authoritySignature;

    @Lob
    @Column(name = "authority_seal", columnDefinition = "LONGTEXT")
    private String authoritySeal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "child", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EpiVaccination> vaccinations;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.approvalStage == null) this.approvalStage = 0;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getChildName() {
		return childName;
	}

	public void setChildName(String childName) {
		this.childName = childName;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getMotherName() {
		return motherName;
	}

	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}

	public String getGuardianNid() {
		return guardianNid;
	}

	public void setGuardianNid(String guardianNid) {
		this.guardianNid = guardianNid;
	}

	public String getFatherNid() {
		return fatherNid;
	}

	public void setFatherNid(String fatherNid) {
		this.fatherNid = fatherNid;
	}

	public String getMotherNid() {
		return motherNid;
	}

	public void setMotherNid(String motherNid) {
		this.motherNid = motherNid;
	}

	public String getGuardianPhone() {
		return guardianPhone;
	}

	public void setGuardianPhone(String guardianPhone) {
		this.guardianPhone = guardianPhone;
	}

	public String getGuardianEmail() { return guardianEmail; }
	public void setGuardianEmail(String guardianEmail) { this.guardianEmail = guardianEmail; }

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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPresentAddress() {
		return presentAddress;
	}

	public void setPresentAddress(String presentAddress) {
		this.presentAddress = presentAddress;
	}

	public String getPermanentAddress() {
		return permanentAddress;
	}

	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}

	public String getBirthPlace() {
		return birthPlace;
	}

	public void setBirthPlace(String birthPlace) {
		this.birthPlace = birthPlace;
	}

	public String getChildPhotoUrl() {
		return childPhotoUrl;
	}

	public void setChildPhotoUrl(String childPhotoUrl) {
		this.childPhotoUrl = childPhotoUrl;
	}

	public String getFatherNidFileUrl() {
		return fatherNidFileUrl;
	}

	public void setFatherNidFileUrl(String fatherNidFileUrl) {
		this.fatherNidFileUrl = fatherNidFileUrl;
	}

	public String getMotherNidFileUrl() {
		return motherNidFileUrl;
	}

	public void setMotherNidFileUrl(String motherNidFileUrl) {
		this.motherNidFileUrl = motherNidFileUrl;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getRegisteredBy() {
		return registeredBy;
	}

	public void setRegisteredBy(String registeredBy) {
		this.registeredBy = registeredBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<EpiVaccination> getVaccinations() {
		return vaccinations;
	}

	public void setVaccinations(List<EpiVaccination> vaccinations) {
		this.vaccinations = vaccinations;
	}

    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
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
    public String getAuthoritySignature() { return authoritySignature; }
    public void setAuthoritySignature(String authoritySignature) { this.authoritySignature = authoritySignature; }
    public String getAuthoritySeal() { return authoritySeal; }
    public void setAuthoritySeal(String authoritySeal) { this.authoritySeal = authoritySeal; }
}
