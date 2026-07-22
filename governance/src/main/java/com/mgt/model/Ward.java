package com.mgt.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "ward")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "ward_number", unique = true, nullable = false)
    private int number;

    @Column(nullable = false)
    private String name;

    private Double area;          
    private Integer population;
    private String representative;
    private String contact;
    private String status = "Active";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Populated by getAllWithBoundaries() — not stored in the ward table itself. */
    @Transient
    private String boundaryGeoJson;
    
    @Override
    public String toString() {
        return "" + number ;
    }
 
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "Active";
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumber() {
		return number;
	}

	// ✅ FK Fix: alias for FK references in HoldingNewRegistration, WardBoundary
	public int getWardNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getArea() {
		return area;
	}

	public void setArea(Double area) {
		this.area = area;
	}

	public Integer getPopulation() {
		return population;
	}

	public void setPopulation(Integer population) {
		this.population = population;
	}

	public String getRepresentative() {
		return representative;
	}

	public void setRepresentative(String representative) {
		this.representative = representative;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
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

	public String getBoundaryGeoJson() {
		return boundaryGeoJson;
	}

	public void setBoundaryGeoJson(String boundaryGeoJson) {
		this.boundaryGeoJson = boundaryGeoJson;
	}

    
}
