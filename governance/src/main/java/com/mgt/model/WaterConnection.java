package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "water_connection")
public class WaterConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String fatherName;
    private String nid;

    private String phone;
    private String email;           // ← নতুন field

    private String district;
    private String upazila;
    private String ward;
    private String address;

    private String connectionType;

    private Integer members;

    @Column(name = "water_usage")
    private String usage;
    private String startDate;
    private String description;

    private Boolean agree;

    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();


    // ── Getters & Setters ──────────────────────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getFatherName()               { return fatherName; }
    public void setFatherName(String fn)        { this.fatherName = fn; }

    public String getNid()                      { return nid; }
    public void setNid(String nid)              { this.nid = nid; }

    public String getPhone()                    { return phone; }
    public void setPhone(String phone)          { this.phone = phone; }

    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }

    public String getDistrict()                 { return district; }
    public void setDistrict(String district)    { this.district = district; }

    public String getUpazila()                  { return upazila; }
    public void setUpazila(String upazila)      { this.upazila = upazila; }

    public String getWard()                     { return ward; }
    public void setWard(String ward)            { this.ward = ward; }

    public String getAddress()                  { return address; }
    public void setAddress(String address)      { this.address = address; }

    public String getConnectionType()           { return connectionType; }
    public void setConnectionType(String ct)    { this.connectionType = ct; }

    public Integer getMembers()                 { return members; }
    public void setMembers(Integer members)     { this.members = members; }

    public String getUsage()                    { return usage; }
    public void setUsage(String usage)          { this.usage = usage; }

    public String getStartDate()                { return startDate; }
    public void setStartDate(String startDate)  { this.startDate = startDate; }

    public String getDescription()              { return description; }
    public void setDescription(String desc)     { this.description = desc; }

    public Boolean getAgree()                   { return agree; }
    public void setAgree(Boolean agree)         { this.agree = agree; }

    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
}
