package com.mgt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "passport_apply")
@Entity(name = "passport")
public class PassportApply {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String fullName;

    @Column(name = "date_of_birth")
    private String dob;

    @Column(name = "place_Of_Birth")
    private String placeOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "maritalStatus")
    private String maritalStatus;

    @Column(name = "religion")
    private String religion;

    @Column(name = "bloodGroup")
    private String bloodGroup;

    @Column(name = "passportType")
    private String passportType;

    @Column(name = "nidNumber")
    private String nidNumber;

    @Column(name = "passportNoPrevious")
    private String passportNoPrevious;

    @Column(name = "currentAddress")
    private String currentAddress;

    @Column(name = "permanentAddress")
    private String permanentAddress;

    @Column(name = "contact")
    private String contact;

    @Column(name = "email")
    private String email;

    @Column(name = "fathersName")
    private String fatherName;

    @Column(name = "mothersName")
    private String motherName;

    @Column(name = "previousTravelCountries")
    private String previousTravelCountries;

    @Column(name = "previousVisaNumbers")
    private String previousVisaNumbers;

    @Column(name = "emergencyContactName")
    private String emergencyContactName;

    @Column(name = "emergencyContactPhone")
    private String emergencyContactPhone;

    @Column(name = "paymentMethod")
    private String paymentMethod;

    @Column(name = "amount")
    private double amount;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "application_date")
    private String applicationDate;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "nid_file_url")
    private String nidFileUrl;

    @Column(name = "birth_file_url")
    private String birthFileUrl;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public String getReligion() {
		return religion;
	}

	public void setReligion(String religion) {
		this.religion = religion;
	}

	public String getBloodGroup() {
		return bloodGroup;
	}

	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}

	public String getPassportType() {
		return passportType;
	}

	public void setPassportType(String passportType) {
		this.passportType = passportType;
	}

	public String getNidNumber() {
		return nidNumber;
	}

	public void setNidNumber(String nidNumber) {
		this.nidNumber = nidNumber;
	}

	public String getPassportNoPrevious() {
		return passportNoPrevious;
	}

	public void setPassportNoPrevious(String passportNoPrevious) {
		this.passportNoPrevious = passportNoPrevious;
	}

	public String getCurrentAddress() {
		return currentAddress;
	}

	public void setCurrentAddress(String currentAddress) {
		this.currentAddress = currentAddress;
	}

	public String getPermanentAddress() {
		return permanentAddress;
	}

	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
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

	public String getPreviousTravelCountries() {
		return previousTravelCountries;
	}

	public void setPreviousTravelCountries(String previousTravelCountries) {
		this.previousTravelCountries = previousTravelCountries;
	}

	public String getPreviousVisaNumbers() {
		return previousVisaNumbers;
	}

	public void setPreviousVisaNumbers(String previousVisaNumbers) {
		this.previousVisaNumbers = previousVisaNumbers;
	}

	public String getEmergencyContactName() {
		return emergencyContactName;
	}

	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = emergencyContactName;
	}

	public String getEmergencyContactPhone() {
		return emergencyContactPhone;
	}

	public void setEmergencyContactPhone(String emergencyContactPhone) {
		this.emergencyContactPhone = emergencyContactPhone;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
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

	public String getApplicationDate() {
		return applicationDate;
	}

	public void setApplicationDate(String applicationDate) {
		this.applicationDate = applicationDate;
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

	public String getBirthFileUrl() {
		return birthFileUrl;
	}

	public void setBirthFileUrl(String birthFileUrl) {
		this.birthFileUrl = birthFileUrl;
	}

}
