package com.mgt.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "epi_vaccination")
public class EpiVaccination {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    @JsonIgnore
    private EpiChild child;

    @Column(name = "vaccine_name", nullable = false)
    private String vaccineName;

    @Column(name = "dose_no", nullable = false)
    private String doseNo;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "given_date")
    private LocalDate givenDate;

    @Column(name = "status")
    private String status = "Scheduled";

    @Column(name = "given_by")
    private String givenBy;

    @Column(name = "health_center")
    private String healthCenter;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status    == null) this.status    = "Scheduled";
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public EpiChild getChild() {
		return child;
	}

	public void setChild(EpiChild child) {
		this.child = child;
	}

	public String getVaccineName() {
		return vaccineName;
	}

	public void setVaccineName(String vaccineName) {
		this.vaccineName = vaccineName;
	}

	public String getDoseNo() {
		return doseNo;
	}

	public void setDoseNo(String doseNo) {
		this.doseNo = doseNo;
	}

	public LocalDate getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(LocalDate scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public LocalDate getGivenDate() {
		return givenDate;
	}

	public void setGivenDate(LocalDate givenDate) {
		this.givenDate = givenDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGivenBy() {
		return givenBy;
	}

	public void setGivenBy(String givenBy) {
		this.givenBy = givenBy;
	}

	public String getHealthCenter() {
		return healthCenter;
	}

	public void setHealthCenter(String healthCenter) {
		this.healthCenter = healthCenter;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public LocalDate getNextDueDate() {
		return nextDueDate;
	}

	public void setNextDueDate(LocalDate nextDueDate) {
		this.nextDueDate = nextDueDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    
}
