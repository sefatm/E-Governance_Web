package com.mgt.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────────────────────
// PasswordResetToken — forgot-password flow এর জন্য secure token
//
// Flow:
//   1. User email submit করে → random UUID token তৈরি হয়, DB তে save
//   2. Email এ link যায়: /reset-password?token=<uuid>
//   3. User link এ ক্লিক করে নতুন password দেয়
//   4. Token validate → password update → token delete
//
// Table: password_reset_token (Hibernate ddl-auto=update তৈরি করবে)
// ─────────────────────────────────────────────────────────────────

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UUID token — email link এ যাবে
    @Column(nullable = false, unique = true)
    private String token;

    // কোন user এর জন্য
    @Column(nullable = false)
    private String email;

    // 30 মিনিট পরে expire
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // token কতবার use হয়েছে তা track (single-use enforce করতে)
    @Column(nullable = false)
    private boolean used = false;

    // ── Constructors ──────────────────────────────────────────────

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email) {
        this.token     = token;
        this.email     = email;
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.used      = false;
    }

    // ── Helper ────────────────────────────────────────────────────

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public String getToken()                { return token; }
    public void setToken(String token)      { this.token = token; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public LocalDateTime getExpiresAt()              { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt){ this.expiresAt = expiresAt; }

    public boolean isUsed()                 { return used; }
    public void setUsed(boolean used)       { this.used = used; }
}
