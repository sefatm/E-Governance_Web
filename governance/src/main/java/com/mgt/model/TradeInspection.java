package com.mgt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * TradeInspection — Physical Inspection Scheduling
 *
 * Workflow:
 *   1. Admin trade license application approve করার আগে inspection schedule করে
 *   2. Inspector নির্দিষ্ট তারিখে business premises এ যায়
 *   3. Inspector outcome লেখে (Passed / Failed + remarks)
 *   4. TradeLicenseService এই result দেখে license approve/reject করে
 *
 * Status lifecycle:
 *   Scheduled → Completed (Passed) → license Approved
 *   Scheduled → Completed (Failed) → license Rejected
 *   Scheduled → Cancelled
 *
 * Table: trade_inspection
 */
@Entity
@Table(name = "trade_inspection")
public class TradeInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** কোন Trade License Application এর জন্য inspection */
    @Column(name = "license_id", nullable = false)
    private int licenseId;

    /** License Number (reference) */
    @Column(name = "license_number")
    private String licenseNumber;

    /** Business এর নাম */
    @Column(name = "business_name")
    private String businessName;

    /** Business এর ঠিকানা (inspection এর location) */
    @Column(name = "business_address")
    private String businessAddress;

    /** কোন Inspector যাবে */
    @Column(name = "inspector_name")
    private String inspectorName;

    /** Inspector এর designation */
    @Column(name = "inspector_designation")
    private String inspectorDesignation;

    /** Inspection এর তারিখ */
    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    /** Inspection এর সময় */
    @Column(name = "inspection_time")
    private LocalTime inspectionTime;

    /**
     * Inspection এর status
     * Scheduled / Completed / Cancelled
     */
    @Column(name = "status")
    private String status = "Scheduled";

    /**
     * Inspection এর outcome (Inspector fill করবে)
     * Passed / Failed
     */
    @Column(name = "outcome")
    private String outcome;

    /** Inspector এর বিস্তারিত remarks */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /** কবে schedule করা হয়েছে */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt = LocalDateTime.now();

    /** Inspection complete হওয়ার সময় */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Applicant এর email — notification এর জন্য */
    @Column(name = "applicant_email")
    private String applicantEmail;

    /** Applicant এর নাম */
    @Column(name = "applicant_name")
    private String applicantName;

    // ─── Getters & Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLicenseId() { return licenseId; }
    public void setLicenseId(int licenseId) { this.licenseId = licenseId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public String getInspectorDesignation() { return inspectorDesignation; }
    public void setInspectorDesignation(String inspectorDesignation) { this.inspectorDesignation = inspectorDesignation; }

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }

    public LocalTime getInspectionTime() { return inspectionTime; }
    public void setInspectionTime(LocalTime inspectionTime) { this.inspectionTime = inspectionTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
}
