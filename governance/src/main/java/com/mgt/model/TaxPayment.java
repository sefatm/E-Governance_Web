package com.mgt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tax_payment")
public class TaxPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "holdingNo", nullable = false)
    private String holdingNo;

    @Column(name = "ownerName", nullable = false)
    private String ownerName;

    @Column(name = "email")
    private String email;

    @Column(name = "amount", nullable = false)
    private Double amount;

    // "bKash" | "Nagad" | "Rocket" | "Debit / Credit Card" | "Bank Transfer"
    @Column(name = "method")
    private String method;

    @Column(name = "txnId")
    private String txnId;

    @Column(name = "paymentDate")
    private LocalDate paymentDate;

    // "Paid" | "Pending" | "Failed"
    @Column(name = "status")
    private String status = "Paid";

    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt   = LocalDateTime.now();
        if (this.paymentDate == null) {
            this.paymentDate = LocalDate.now();
        }
    }

    // ── Getters & Setters ────────────────────────────────────

    public int getId()                { return id; }
    public void setId(int id)         { this.id = id; }

    public String getHoldingNo()              { return holdingNo; }
    public void setHoldingNo(String holdingNo){ this.holdingNo = holdingNo; }

    public String getOwnerName()              { return ownerName; }
    public void setOwnerName(String ownerName){ this.ownerName = ownerName; }

    public Double getAmount()               { return amount; }
    public void setAmount(Double amount)    { this.amount = amount; }

    public String getMethod()               { return method; }
    public void setMethod(String method)    { this.method = method; }

    public String getTxnId()                { return txnId; }
    public void setTxnId(String txnId)      { this.txnId = txnId; }

    public LocalDate getPaymentDate()                   { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate)   { this.paymentDate = paymentDate; }

    public String getStatus()               { return status; }
    public void setStatus(String status)    { this.status = status; }

    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
