package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "etender_bid")
public class ETenderBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "tender_id")
    private int tenderId;

    @Column(name = "bidder_name")
    private String bidderName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "nid")
    private String nid;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "email")
    private String email;

    @Column(name = "bid_amount")
    private Double bidAmount;

    @Column(name = "completion_days")
    private int completionDays;

    @Column(name = "experience_years")
    private int experienceYears;

    @Column(name = "previous_works")
    private String previousWorks;

    @Column(name = "emd_receipt_no")
    private String emdReceiptNo;

    // ── নতুন: Bidder uploaded document ───────────────────────────────────────
    /** Bidder এর uploaded document এর path (trade license, tax cert, etc.) */
    @Column(name = "document_url")
    private String documentUrl;

    // ── নতুন: Document Verification ──────────────────────────────────────────
    /**
     * Admin document verify করেছে কিনা
     * null    = এখনো review হয়নি
     * true    = Verified ✅
     * false   = Rejected ❌
     */
    @Column(name = "doc_verified")
    private Boolean docVerified;

    /**
     * Admin এর verification note
     * Rejected হলে কারণ লিখবে, Verified হলে note রাখতে পারবে
     */
    @Column(name = "doc_remark")
    private String docRemark;

    // ── নতুন: Lowest Bid Flag ─────────────────────────────────────────────────
    /**
     * এই bid টা tender এ সবচেয়ে কম কিনা
     * ETenderBidService.recalculateLowest() call করলে auto-update হয়
     */
    @Column(name = "is_lowest")
    private Boolean isLowest = false;

    // ── Existing fields ───────────────────────────────────────────────────────
    @Column(name = "status")
    private String status = "Submitted";

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    // ─── Getters & Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Double getBidAmount() { return bidAmount; }
    public void setBidAmount(Double bidAmount) { this.bidAmount = bidAmount; }

    public int getCompletionDays() { return completionDays; }
    public void setCompletionDays(int completionDays) { this.completionDays = completionDays; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public String getPreviousWorks() { return previousWorks; }
    public void setPreviousWorks(String previousWorks) { this.previousWorks = previousWorks; }

    public String getEmdReceiptNo() { return emdReceiptNo; }
    public void setEmdReceiptNo(String emdReceiptNo) { this.emdReceiptNo = emdReceiptNo; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public Boolean getDocVerified() { return docVerified; }
    public void setDocVerified(Boolean docVerified) { this.docVerified = docVerified; }

    public String getDocRemark() { return docRemark; }
    public void setDocRemark(String docRemark) { this.docRemark = docRemark; }

    public Boolean isLowest() { return isLowest; }
    public void setLowest(Boolean lowest) { isLowest = lowest; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
