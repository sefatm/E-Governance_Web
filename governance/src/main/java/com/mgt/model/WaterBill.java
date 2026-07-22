package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;
import jakarta.persistence.Column;

@Entity
@Table(name = "water_bill")
public class WaterBill {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String email;
    private String mobile;
    private String nid;
    private String meterNo;
    private String month;

    private int previousReading;
    private int currentReading;

    private int units;
    private double amount;

    private String connectionType;
    private String status;
    private String billType;
    private String paymentMethod;
    private String txnRef;
    private String receiptNo;
    private LocalDateTime paidAt;
    
    @Lob
    @Column(name = "authority_signature", columnDefinition = "LONGTEXT")
    private String authoritySignature;

    @Lob
    @Column(name = "authority_seal", columnDefinition = "LONGTEXT")
    private String authoritySeal;
    
    private LocalDateTime createdAt = LocalDateTime.now();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getMeterNo() {
		return meterNo;
	}

	public void setMeterNo(String meterNo) {
		this.meterNo = meterNo;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getPreviousReading() {
		return previousReading;
	}

	public void setPreviousReading(int previousReading) {
		this.previousReading = previousReading;
	}

	public int getCurrentReading() {
		return currentReading;
	}

	public void setCurrentReading(int currentReading) {
		this.currentReading = currentReading;
	}

	public int getUnits() {
		return units;
	}

	public void setUnits(int units) {
		this.units = units;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
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

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}
    
	public String getMobile() { return mobile; }
	public void setMobile(String mobile) { this.mobile = mobile; }
	public String getNid() { return nid; }
	public void setNid(String nid) { this.nid = nid; }
	public String getPaymentMethod() { return paymentMethod; }
	public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
	public String getTxnRef() { return txnRef; }
	public void setTxnRef(String txnRef) { this.txnRef = txnRef; }
	public String getReceiptNo() { return receiptNo; }
	public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
	public LocalDateTime getPaidAt() { return paidAt; }
	public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
	public String getAuthoritySignature() { return authoritySignature; }
	public void setAuthoritySignature(String authoritySignature) { this.authoritySignature = authoritySignature; }
	public String getAuthoritySeal() { return authoritySeal; }
	public void setAuthoritySeal(String authoritySeal) { this.authoritySeal = authoritySeal; }

}
