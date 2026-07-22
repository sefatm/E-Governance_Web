package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_receipt")
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "receipt_no", unique = true)
    private String receiptNo;

    @Column(name = "txn_id", unique = true)
    private int txnId;

    @Column(name = "citizen_nid")
    private String citizenNid;

    @Column(name = "citizen_name")
    private String citizenName;

    @Column(name = "service_type")
    private String serviceType;

    private String description;
    private double amount;
    private String method;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt = LocalDateTime.now();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReceiptNo() {
		return receiptNo;
	}

	public void setReceiptNo(String receiptNo) {
		this.receiptNo = receiptNo;
	}

	public int getTxnId() {
		return txnId;
	}

	public void setTxnId(int txnId) {
		this.txnId = txnId;
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

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public LocalDateTime getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(LocalDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

   
}
