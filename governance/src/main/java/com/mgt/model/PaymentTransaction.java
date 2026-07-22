package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "txn_ref", unique = true)
    private String txnRef;

    @Column(name = "citizen_nid")
    private String citizenNid;

    @Column(name = "citizen_name")
    private String citizenName;

    private String mobile;
    private String email;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "service_ref_id")
    private Integer serviceRefId;

    private String description;

    // FIX: Store holdingNo directly to avoid fragile description parsing in PaymentController
    @Column(name = "holding_no")
    private String holdingNo;

    private double amount;
    private String method;

    @Column(name = "provider_txn_id")
    private String providerTxnId;

    @Column(name = "card_last4")
    private String cardLast4;

    private String status = "Pending";

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTxnRef() {
		return txnRef;
	}

	public void setTxnRef(String txnRef) {
		this.txnRef = txnRef;
	}

	public String getCitizenNid() {
		return citizenNid;
	}

	public void setCitizenNid(String citizenNid) {
		this.citizenNid = citizenNid;
	}

	public String getCitizenName() {
		return citizenName;
	}

	public void setCitizenName(String citizenName) {
		this.citizenName = citizenName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public Integer getServiceRefId() {
		return serviceRefId;
	}

	public void setServiceRefId(Integer serviceRefId) {
		this.serviceRefId = serviceRefId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHoldingNo() {
		return holdingNo;
	}

	public void setHoldingNo(String holdingNo) {
		this.holdingNo = holdingNo;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getProviderTxnId() {
		return providerTxnId;
	}

	public void setProviderTxnId(String providerTxnId) {
		this.providerTxnId = providerTxnId;
	}

	public String getCardLast4() {
		return cardLast4;
	}

	public void setCardLast4(String cardLast4) {
		this.cardLast4 = cardLast4;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
