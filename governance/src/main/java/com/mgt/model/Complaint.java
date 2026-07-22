package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Table;

@Table(name = "complaints")
@Entity(name = "complaint")
public class Complaint {

	    @Id
	    @Column(name = "id")
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;

	    @Column(name = "name")
	    private String name;
	    
	    @Column(name = "ward")
	    private String ward;
	    
	    @Column(name = "area")
	    private String area;
	    
	    @Column(name = "category")
	    private String category;
	    
	    @Column(name = "description")
	    private String description;
	    
	    @Column(name = "contact")
	    private String contact;

	    @Column(name = "email")
	    private String email;
	    
	    @Column(name = "location")
	    private String location;
	    
	    @Column(name = "imageUrl")
	    private String imageUrl;
	    
	    @Column(name = "remarks")
	    private String remarks;
	    
	    @Column(name = "status")
	    private String status;

	    @Column(name = "lat")
        private Double lat;

        @Column(name = "lng")
        private Double lng;

        @Column(name = "createdAt")
        private LocalDateTime createdAt = LocalDateTime.now();

	    public String getRemarks() {
			return remarks;
		}

		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}

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

		public String getWard() {
			return ward;
		}

		public void setWard(String ward) {
			this.ward = ward;
		}

		public String getArea() {
			return area;
		}

		public void setArea(String area) {
			this.area = area;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getContact() {
			return contact;
		}

		public void setContact(String contact) {
			this.contact = contact;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
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
	    
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
	    
}
