package com.mgt.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sanitation")
public class Sanitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;        // ← নতুন
    private String email;       // ← নতুন

    private String area;
    private String issue;
    private String description;
    private String status;
    private String date;
    private LocalDate appliedDate = LocalDate.now();

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public String getName()                         { return name; }
    public void setName(String name)                { this.name = name; }

    public String getEmail()                        { return email; }
    public void setEmail(String email)              { this.email = email; }

    public String getArea()                         { return area; }
    public void setArea(String area)                { this.area = area; }

    public String getIssue()                        { return issue; }
    public void setIssue(String issue)              { this.issue = issue; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public String getStatus()                       { return status; }
    public void setStatus(String status)            { this.status = status; }

    public String getDate()                         { return date; }
    public void setDate(String date)                { this.date = date; }

    public LocalDate getAppliedDate()               { return appliedDate; }
    public void setAppliedDate(LocalDate d)         { this.appliedDate = d; }
}
