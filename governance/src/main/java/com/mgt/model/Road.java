package com.mgt.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "road")
@Entity(name = "road")
public class Road {

	@Id
 	@Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	
	private String name;
    private String nid;
    private String contact;

    private String district;
    private String upazila;
    private String ward;
    private String area;

    private String roadName;
    private String type;
    @Column(name = "road_condition")
    private String roadCondition;

    private Double length;
    private Double width;

    @Column(length = 2000)
    private String description;

    private String priority;

    private String status;

    @Column(name = "date")
    private LocalDate appliedDate = LocalDate.now();
    
    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

	public Double getLat() { return lat; }
	public void setLat(Double lat) { this.lat = lat; }
	public Double getLng() { return lng; }
	public void setLng(Double lng) { this.lng = lng; }

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getNid() { return nid; }
	public void setNid(String nid) { this.nid = nid; }
	public String getContact() { return contact; }
	public void setContact(String contact) { this.contact = contact; }
	public String getDistrict() { return district; }
	public void setDistrict(String district) { this.district = district; }
	public String getUpazila() { return upazila; }
	public void setUpazila(String upazila) { this.upazila = upazila; }
	public String getWard() { return ward; }
	public void setWard(String ward) { this.ward = ward; }
	public String getArea() { return area; }
	public void setArea(String area) { this.area = area; }
	public String getRoadName() { return roadName; }
	public void setRoadName(String roadName) { this.roadName = roadName; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public String getRoadCondition() { return roadCondition; }
	public void setRoadCondition(String roadCondition) { this.roadCondition = roadCondition; }
	public Double getLength() { return length; }
	public void setLength(Double length) { this.length = length; }
	public Double getWidth() { return width; }
	public void setWidth(Double width) { this.width = width; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getPriority() { return priority; }
	public void setPriority(String priority) { this.priority = priority; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDate getAppliedDate() { return appliedDate; }
	public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }
}
