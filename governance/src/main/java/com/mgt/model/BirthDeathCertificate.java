package com.mgt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "birth_death_certificate")
@Entity
public class BirthDeathCertificate {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String type;

    private String certificateNo;

    private String name;
    private String nameBn; 
    private String dob;
    private String placeOfBirth;
    @Column(name = "gender_of_birth")
    private String genderOfBirth;

    private String address;
    private String contact;
    private String email;
    private String mobileNumber;

    private String fathersName;
    private String fathersDob;
    private String fathersNid;
    private String fathersEmail;
    private String fathersContact;

    private String mothersName;
    private String mothersDob;
    private String mothersNid;
    private String mothersEmail;
    private String mothersContact;

    private String permanentAddress;
    private String emergencyName;
    private String emergencyPhone;
    private String paymentMethod;
    private String amount;
    private String issueDate;
    
    private String dateOfDeath;
    private String placeOfDeath;
    private String gender;
    private String nid;
    private String birthNo;
    private String applicantName;
    private String relation;
    private String status = "Pending";

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

    private LocalDateTime createdAt = LocalDateTime.now();

    private String fatherNidFileUrl;  
    private String motherNidFileUrl;
    private String vaccineFileUrl;    
    private String deathNidFileUrl;
    private String medicalFileUrl;

    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCertificateNo() {
		return certificateNo;
	}
	public void setCertificateNo(String certificateNo) {
		this.certificateNo = certificateNo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getPlaceOfBirth() {
		return placeOfBirth;
	}
	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}
	public String getGenderOfBirth() {
		return genderOfBirth;
	}
	public void setGenderOfBirth(String genderOfBirth) {
		this.genderOfBirth = genderOfBirth;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
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
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getFathersName() {
		return fathersName;
	}
	public void setFathersName(String fathersName) {
		this.fathersName = fathersName;
	}
	public String getFathersDob() {
		return fathersDob;
	}
	public void setFathersDob(String fathersDob) {
		this.fathersDob = fathersDob;
	}
	public String getFathersNid() {
		return fathersNid;
	}
	public void setFathersNid(String fathersNid) {
		this.fathersNid = fathersNid;
	}
	public String getFathersEmail() {
		return fathersEmail;
	}
	public void setFathersEmail(String fathersEmail) {
		this.fathersEmail = fathersEmail;
	}
	public String getFathersContact() {
		return fathersContact;
	}
	public void setFathersContact(String fathersContact) {
		this.fathersContact = fathersContact;
	}
	public String getMothersName() {
		return mothersName;
	}
	public void setMothersName(String mothersName) {
		this.mothersName = mothersName;
	}
	public String getMothersDob() {
		return mothersDob;
	}
	public void setMothersDob(String mothersDob) {
		this.mothersDob = mothersDob;
	}
	public String getMothersNid() {
		return mothersNid;
	}
	public void setMothersNid(String mothersNid) {
		this.mothersNid = mothersNid;
	}
	public String getMothersEmail() {
		return mothersEmail;
	}
	public void setMothersEmail(String mothersEmail) {
		this.mothersEmail = mothersEmail;
	}
	public String getMothersContact() {
		return mothersContact;
	}
	public void setMothersContact(String mothersContact) {
		this.mothersContact = mothersContact;
	}
	public String getPermanentAddress() {
		return permanentAddress;
	}
	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}
	public String getEmergencyName() {
		return emergencyName;
	}
	public void setEmergencyName(String emergencyName) {
		this.emergencyName = emergencyName;
	}
	public String getEmergencyPhone() {
		return emergencyPhone;
	}
	public void setEmergencyPhone(String emergencyPhone) {
		this.emergencyPhone = emergencyPhone;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getIssueDate() {
		return issueDate;
	}
	public void setIssueDate(String issueDate) {
		this.issueDate = issueDate;
	}
	public String getDateOfDeath() {
		return dateOfDeath;
	}
	public void setDateOfDeath(String dateOfDeath) {
		this.dateOfDeath = dateOfDeath;
	}
	public String getPlaceOfDeath() {
		return placeOfDeath;
	}
	public void setPlaceOfDeath(String placeOfDeath) {
		this.placeOfDeath = placeOfDeath;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getNid() {
		return nid;
	}
	public void setNid(String nid) {
		this.nid = nid;
	}
	public String getBirthNo() {
		return birthNo;
	}
	public void setBirthNo(String birthNo) {
		this.birthNo = birthNo;
	}
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
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
	public String getNameBn() {
		return nameBn;
	}
	public void setNameBn(String nameBn) {
		this.nameBn = nameBn;
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
	public String getVaccineFileUrl() {
		return vaccineFileUrl;
	}
	public void setVaccineFileUrl(String vaccineFileUrl) {
		this.vaccineFileUrl = vaccineFileUrl;
	}
	public String getDeathNidFileUrl() {
		return deathNidFileUrl;
	}
	public void setDeathNidFileUrl(String deathNidFileUrl) {
		this.deathNidFileUrl = deathNidFileUrl;
	}
	public String getMedicalFileUrl() {
		return medicalFileUrl;
	}
	public void setMedicalFileUrl(String medicalFileUrl) {
		this.medicalFileUrl = medicalFileUrl;
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
