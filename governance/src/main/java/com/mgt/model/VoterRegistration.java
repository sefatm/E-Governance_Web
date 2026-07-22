package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "voter_registration",
       uniqueConstraints = @UniqueConstraint(name = "UKvoter_nid", columnNames = "nid"))
@NoArgsConstructor
@AllArgsConstructor
public class VoterRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "date_of_birth")
    private String dob;

    private String gender;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;

    private String nid;

    @Column(name = "mobile_number")
    private String mobile;

    private String email;           

    private String district;
    private String upazila;
    private String area;
    private String address;

    @Column(name = "election_type")
    private String electionType;

    @Column(name = "photo_url")
    private String photoUrl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_voter_zone"))
    private VotingZone zone;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_voter_center"))
    private VotingCenter center;

    @Column(name = "registration_date")
    private String registrationDate;

    private String status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public Integer getId()                          { return id; }
    public void    setId(Integer id)                { this.id = id; }
    public String  getName()                        { return name; }
    public void    setName(String name)             { this.name = name; }
    public String  getDob()                         { return dob; }
    public void    setDob(String dob)               { this.dob = dob; }
    public String  getGender()                      { return gender; }
    public void    setGender(String gender)         { this.gender = gender; }
    public String  getFatherName()                  { return fatherName; }
    public void    setFatherName(String v)          { this.fatherName = v; }
    public String  getMotherName()                  { return motherName; }
    public void    setMotherName(String v)          { this.motherName = v; }
    public String  getNid()                         { return nid; }
    public void    setNid(String nid)               { this.nid = nid; }
    public String  getMobile()                      { return mobile; }
    public void    setMobile(String mobile)         { this.mobile = mobile; }
    public String  getEmail()                       { return email; }
    public void    setEmail(String email)           { this.email = email; }
    public String  getDistrict()                    { return district; }
    public void    setDistrict(String district)     { this.district = district; }
    public String  getUpazila()                     { return upazila; }
    public void    setUpazila(String upazila)       { this.upazila = upazila; }
    public String  getArea()                        { return area; }
    public void    setArea(String area)             { this.area = area; }
    public String  getAddress()                     { return address; }
    public void    setAddress(String address)       { this.address = address; }
    public String  getElectionType()                { return electionType; }
    public void    setElectionType(String v)        { this.electionType = v; }
    public String  getPhotoUrl()                    { return photoUrl; }
    public void    setPhotoUrl(String photoUrl)     { this.photoUrl = photoUrl; }
    public VotingZone   getZone()                   { return zone; }
    public void         setZone(VotingZone zone)    { this.zone = zone; }
    public Integer      getZoneId()                 { return zone != null ? zone.getId() : null; }
    public VotingCenter getCenter()                 { return center; }
    public void         setCenter(VotingCenter c)   { this.center = c; }
    public Integer      getCenterId()               { return center != null ? center.getId() : null; }
    public String  getRegistrationDate()            { return registrationDate; }
    public void    setRegistrationDate(String v)    { this.registrationDate = v; }
    public String  getStatus()                      { return status; }
    public void    setStatus(String status)         { this.status = status; }
    public String  getRejectReason()                { return rejectReason; }
    public void    setRejectReason(String v)        { this.rejectReason = v; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void    setCreatedAt(LocalDateTime v)    { this.createdAt = v; }
}
