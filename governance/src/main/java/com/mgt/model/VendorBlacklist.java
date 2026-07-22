package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Vendor Blacklist — কোনো vendor কে block করার জন্য
 *
 * Block করার basis:
 *   - nid        → NID number দিয়ে identify
 *   - email      → email দিয়ে identify
 *   - mobile     → mobile দিয়ে identify
 *   (যেকোনো একটা match হলেই bid block হবে)
 *
 * Table: vendor_blacklist
 */
@Entity
@Table(name = "vendor_blacklist")
public class VendorBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Blacklisted vendor এর NID — primary identifier */
    @Column(name = "nid")
    private String nid;

    /** Blacklisted vendor এর email */
    @Column(name = "email")
    private String email;

    /** Blacklisted vendor এর mobile */
    @Column(name = "mobile")
    private String mobile;

    /** Vendor এর নাম (reference এর জন্য) */
    @Column(name = "vendor_name")
    private String vendorName;

    /** Company name */
    @Column(name = "company_name")
    private String companyName;

    /** কেন blacklist করা হয়েছে */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /** কে blacklist করেছে (admin name/id) */
    @Column(name = "blacklisted_by")
    private String blacklistedBy;

    /** কবে blacklist করা হয়েছে */
    @Column(name = "blacklisted_at")
    private LocalDateTime blacklistedAt = LocalDateTime.now();

    /** Active = true মানে এখনও blocked */
    @Column(name = "is_active")
    private boolean active = true;

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getBlacklistedBy() { return blacklistedBy; }
    public void setBlacklistedBy(String blacklistedBy) { this.blacklistedBy = blacklistedBy; }

    public LocalDateTime getBlacklistedAt() { return blacklistedAt; }
    public void setBlacklistedAt(LocalDateTime blacklistedAt) { this.blacklistedAt = blacklistedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
