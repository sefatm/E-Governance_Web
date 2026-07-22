package com.mgt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "citizen_certificate")
@Entity(name = "citizen")
public class CitizenCertificate {

	 	@Id
	 	@Column(name = "id")
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;

	    @Column(name = "full_name")
	    private String name;

	    @Column(name = "father_name")
	    private String fatherName;

	    @Column(name = "mother_name")
	    private String motherName;

	    @Column(name = "nid", nullable = false, unique = true)
	    private String nid;

	    @Column(name = "date_of_birth")
	    private String dateOfBirth;

	    @Column(name = "gender")
	    private String gender;

	    @Column(name = "blood_group")
	    private String bloodGroup;

	    @Column(name = "religion")
	    private String religion;

	    @Column(name = "marital_status")
	    private String maritalStatus;

	    @Column(name = "occupation")
	    private String occupation;

	    @Column(name = "contact", nullable = false)
	    private String contact;

	    @Column(name = "email")
	    private String email;

	    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
	    private String address;

	    @Column(name = "permanent_address", columnDefinition = "TEXT")
	    private String permanentAddress;

	    @Column(name = "division")
	    private String division;

	    @Column(name = "district")
	    private String district;

	    @Column(name = "certificate_type")
	    private String certificateType;
	    
	    @Column(name = "certificate_no")
	    private String certificateNo;

		@Column(name = "purpose")
	    private String purpose;

	    @Column(name = "declaration")
	    private Boolean declaration;

	    @Column(name = "photo_url")
	    private String photoUrl;

	    @Column(name = "nid_file_url")
	    private String nidFileUrl;

	    @Column(name = "status")
	    private String status;
	    
	
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

    @Column(name = "created_at")
	    private LocalDateTime createdAt = LocalDateTime.now();

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
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

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public String getBloodGroup() {
			return bloodGroup;
		}

		public void setBloodGroup(String bloodGroup) {
			this.bloodGroup = bloodGroup;
		}

		public String getReligion() {
			return religion;
		}

		public void setReligion(String religion) {
			this.religion = religion;
		}

		public String getMaritalStatus() {
			return maritalStatus;
		}

		public void setMaritalStatus(String maritalStatus) {
			this.maritalStatus = maritalStatus;
		}

		public String getOccupation() {
			return occupation;
		}

		public void setOccupation(String occupation) {
			this.occupation = occupation;
		}

		public String getContact() {
			return contact;
		}

		public void setContact(String contact) {
			this.contact = contact;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
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
		
		public String getCertificateNo() {
			return certificateNo;
		}

		public void setCertificateNo(String certificateNo) {
			this.certificateNo = certificateNo;
		}

		public String getCertificateType() {
			return certificateType;
		}

		public void setCertificateType(String certificateType) {
			this.certificateType = certificateType;
		}

		public String getPurpose() {
			return purpose;
		}

		public void setPurpose(String purpose) {
			this.purpose = purpose;
		}

		public Boolean getDeclaration() {
			return declaration;
		}

		public void setDeclaration(Boolean declaration) {
			this.declaration = declaration;
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
