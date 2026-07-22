package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_message")
public class NotificationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String type;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "recipient_type")
    private String recipientType;

    @Column(name = "recipient_val")
    private String recipientVal;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "sent_by")
    private String sentBy = "Admin";

    private String status = "Sent";

    @Column(name = "service_tag")
    private String serviceTag;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(String recipientType) {
		this.recipientType = recipientType;
	}

	public String getRecipientVal() {
		return recipientVal;
	}

	public void setRecipientVal(String recipientVal) {
		this.recipientVal = recipientVal;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public String getSentBy() {
		return sentBy;
	}

	public void setSentBy(String sentBy) {
		this.sentBy = sentBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getServiceTag() {
		return serviceTag;
	}

	public void setServiceTag(String serviceTag) {
		this.serviceTag = serviceTag;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    
}
