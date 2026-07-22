package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Table(name = "holding_new_registration")
@Entity
public class HoldingNewRegistration {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "applicant_name")     private String applicantName;
    @Column(name = "fathers_name")       private String father;
    @Column(name = "mothers_name")       private String mother;
    @Column(name = "nid")               private String nid;
    @Column(name = "holding_no")         private String holdingNo;
    @Column(name = "previous_holding_no") private String previousHoldingNo;
    @Column(name = "road")              private String road;
    @Column(name = "area")              private String area;
    @Column(name = "mouza")             private String mouza;

    // ✅ FK: holding_new_registration.ward → ward.ward_number
    // ward column int — Ward.ward_number তে UNIQUE KEY আছে
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward", referencedColumnName = "ward_number",
                foreignKey = @ForeignKey(name = "fk_holding_ward"))
    private Ward ward;

    @Column(name = "land_size")          private Double landSize;
    @Column(name = "structure_type")     private String structureType;
    @Column(name = "rooms")             private Integer rooms;
    @Column(name = "floors_tin")         private Integer floorsTin;
    @Column(name = "floors_paka")        private Integer floorsPaka;
    @Column(name = "unit_per_floor")      private Integer unitsPerFloor;
    @Column(name = "area_per_floor")      private Double areaPerFloor;
    @Column(name = "construction_year")  private Integer constructionYear;
    @Column(name = "ownership")         private String ownership;
    @Column(name = "usage_type")         private String usageType;
    @Column(name = "deed_copy")          private Boolean deedCopy;
    @Column(name = "mutation_copy")      private Boolean mutationCopy;
    @Column(name = "nid_copy")           private Boolean nidCopy;
    @Column(name = "citizenship")       private Boolean citizenship;
    @Column(name = "contact_name")       private String contactName;
    @Column(name = "mobile_number")            private String mobile;
    @Column(name = "email")             private String email;
    @Column(name = "address")           private String address;
    @Column(name = "status")            private String status = "Pending";

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
    @Column(name = "nid_file_url")      private String nidFileUrl;
    @Column(name = "deed_file_url")     private String deedFileUrl;
    @Column(name = "photo_url")         private String photoUrl;
    @Column(name = "latitude")          private Double latitude;
    @Column(name = "longitude")         private Double longitude;

	@Column(name = "tax_status")
    private String taxStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.approvalStage == null) this.approvalStage = 0;
        if (this.status == null) this.status = "Pending";
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int    getId()                               { return id; }
    public void   setId(int id)                         { this.id = id; }

    public String getApplicantName()                    { return applicantName; }
    public void   setApplicantName(String v)            { this.applicantName = v; }

    public String getFather()                           { return father; }
    public void   setFather(String v)                   { this.father = v; }

    public String getMother()                           { return mother; }
    public void   setMother(String v)                   { this.mother = v; }

    public String getNid()                              { return nid; }
    public void   setNid(String nid)                    { this.nid = nid; }

    public String getHoldingNo()                        { return holdingNo; }
    public void   setHoldingNo(String v)                { this.holdingNo = v; }

    public String getPreviousHoldingNo()                { return previousHoldingNo; }
    public void   setPreviousHoldingNo(String v)        { this.previousHoldingNo = v; }

    public String getRoad()                             { return road; }
    public void   setRoad(String v)                     { this.road = v; }

    public String getArea()                             { return area; }
    public void   setArea(String v)                     { this.area = v; }

    public String getMouza()                            { return mouza; }
    public void   setMouza(String v)                    { this.mouza = v; }

    public Ward   getWard()                             { return ward; }
    public void   setWard(Ward ward)                    { this.ward = ward; }
    // Backward-compatible int getter (controllers এ getWard() int চাইলে)
    public int    getWardNumber() { return ward != null ? ward.getWardNumber() : 0; }

    public Double   getLandSize()                       { return landSize; }
    public void     setLandSize(Double v)               { this.landSize = v; }

    public String   getStructureType()                  { return structureType; }
    public void     setStructureType(String v)          { this.structureType = v; }

    public Integer  getRooms()                          { return rooms; }
    public void     setRooms(Integer v)                 { this.rooms = v; }

    public Integer  getFloorsTin()                      { return floorsTin; }
    public void     setFloorsTin(Integer v)             { this.floorsTin = v; }

    public Integer  getFloorsPaka()                     { return floorsPaka; }
    public void     setFloorsPaka(Integer v)            { this.floorsPaka = v; }

    public Integer  getUnitsPerFloor()                  { return unitsPerFloor; }
    public void     setUnitsPerFloor(Integer v)         { this.unitsPerFloor = v; }

    public Double   getAreaPerFloor()                   { return areaPerFloor; }
    public void     setAreaPerFloor(Double v)           { this.areaPerFloor = v; }

    public Integer  getConstructionYear()               { return constructionYear; }
    public void     setConstructionYear(Integer v)      { this.constructionYear = v; }

    public String   getOwnership()                      { return ownership; }
    public void     setOwnership(String v)              { this.ownership = v; }

    public String   getUsageType()                      { return usageType; }
    public void     setUsageType(String v)              { this.usageType = v; }

    public Boolean  getDeedCopy()                       { return deedCopy; }
    public void     setDeedCopy(Boolean v)              { this.deedCopy = v; }

    public Boolean  getMutationCopy()                   { return mutationCopy; }
    public void     setMutationCopy(Boolean v)          { this.mutationCopy = v; }

    public Boolean  getNidCopy()                        { return nidCopy; }
    public void     setNidCopy(Boolean v)               { this.nidCopy = v; }

    public Boolean  getCitizenship()                    { return citizenship; }
    public void     setCitizenship(Boolean v)           { this.citizenship = v; }

    public String   getContactName()                    { return contactName; }
    public void     setContactName(String v)            { this.contactName = v; }

    public String   getMobile()                         { return mobile; }
    public void     setMobile(String v)                 { this.mobile = v; }

    public String   getEmail()                          { return email; }
    public void     setEmail(String v)                  { this.email = v; }

    public String   getAddress()                        { return address; }
    public void     setAddress(String v)                { this.address = v; }

    public String   getStatus()                         { return status; }
    public void     setStatus(String v)                 { this.status = v; }

    public String   getNidFileUrl()                     { return nidFileUrl; }
    public void     setNidFileUrl(String v)             { this.nidFileUrl = v; }

    public String   getDeedFileUrl()                    { return deedFileUrl; }
    public void     setDeedFileUrl(String v)            { this.deedFileUrl = v; }

    public String   getPhotoUrl()                       { return photoUrl; }
    public void     setPhotoUrl(String v)               { this.photoUrl = v; }

    public Double   getLatitude()                       { return latitude; }
    public void     setLatitude(Double v)               { this.latitude = v; }

    public Double   getLongitude()                      { return longitude; }
    public void     setLongitude(Double v)              { this.longitude = v; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
    
    public Integer getApprovalStage() { return approvalStage; }
    public void setApprovalStage(Integer v) { this.approvalStage = v; }
    public String getFirstApprovedBy() { return firstApprovedBy; }
    public void setFirstApprovedBy(String v) { this.firstApprovedBy = v; }
    public String getFirstSignature() { return firstSignature; }
    public void setFirstSignature(String v) { this.firstSignature = v; }
    public String getFirstSeal() { return firstSeal; }
    public void setFirstSeal(String v) { this.firstSeal = v; }
    public LocalDateTime getFirstApprovedAt() { return firstApprovedAt; }
    public void setFirstApprovedAt(LocalDateTime v) { this.firstApprovedAt = v; }
    public String getSecondApprovedBy() { return secondApprovedBy; }
    public void setSecondApprovedBy(String v) { this.secondApprovedBy = v; }
    public String getSecondSignature() { return secondSignature; }
    public void setSecondSignature(String v) { this.secondSignature = v; }
    public String getSecondSeal() { return secondSeal; }
    public void setSecondSeal(String v) { this.secondSeal = v; }
    public LocalDateTime getSecondApprovedAt() { return secondApprovedAt; }
    public void setSecondApprovedAt(LocalDateTime v) { this.secondApprovedAt = v; }

    public String getTaxStatus() {
		return taxStatus;
	}

	public void setTaxStatus(String taxStatus) {
		this.taxStatus = taxStatus;
	}
}
