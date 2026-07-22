package com.mgt.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nominee")
@NoArgsConstructor
@AllArgsConstructor
public class Nominee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "fathers_name")
    private String fathersName;

    @Column(name = "mothers_name")
    private String mothersName;

    private String nid;

    @Column(name = "mobile_number")
    private String mobileNumber;

    private String dob;

    @Column(name = "election_type")
    private String electionType;

    private String area;
    private String party;
    private String symbol;

    @Column(name = "symbol_file_url")
    private String symbolFileUrl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = true,
                foreignKey = @ForeignKey(name = "fk_nominee_zone"))
    private VotingZone zone;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = true,
                foreignKey = @ForeignKey(name = "fk_nominee_center"))
    private VotingCenter center;

    private String declaration;

    @Column(name = "has_criminal_record")
    private Boolean hasCriminalRecord = false;

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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFathersName() {
		return fathersName;
	}

	public void setFathersName(String fathersName) {
		this.fathersName = fathersName;
	}

	public String getMothersName() {
		return mothersName;
	}

	public void setMothersName(String mothersName) {
		this.mothersName = mothersName;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getElectionType() {
		return electionType;
	}

	public void setElectionType(String electionType) {
		this.electionType = electionType;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getParty() {
		return party;
	}

	public void setParty(String party) {
		this.party = party;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbolFileUrl() {
		return symbolFileUrl;
	}

	public void setSymbolFileUrl(String symbolFileUrl) {
		this.symbolFileUrl = symbolFileUrl;
	}

	public VotingZone getZone() {
		return zone;
	}

	public void setZone(VotingZone zone) {
		this.zone = zone;
	}

	public VotingCenter getCenter() {
		return center;
	}

	public void setCenter(VotingCenter center) {
		this.center = center;
	}

	public String getDeclaration() {
		return declaration;
	}

	public void setDeclaration(String declaration) {
		this.declaration = declaration;
	}

	public Boolean getHasCriminalRecord() {
		return hasCriminalRecord;
	}

	public void setHasCriminalRecord(Boolean hasCriminalRecord) {
		this.hasCriminalRecord = hasCriminalRecord;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

   
}
