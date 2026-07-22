package com.mgt.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "smart_bin")
public class SmartBin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name="bin_code", unique=true, nullable=false)
    private String binCode;
    private String location;
    private String ward;
    @Column(name="bin_type") private String binType;
    @Column(name="fill_level") private Integer fillLevel = 0;
    private String status = "Normal";
    private Double lat;
    private Double lng;
    @Column(name="last_collected") private LocalDateTime lastCollected;
    @Column(name="last_updated") private LocalDateTime lastUpdated = LocalDateTime.now();
    public Integer getId(){return id;} public void setId(Integer id){this.id=id;}
    public String getBinCode(){return binCode;} public void setBinCode(String v){this.binCode=v;}
    public String getLocation(){return location;} public void setLocation(String v){this.location=v;}
    public String getWard(){return ward;} public void setWard(String v){this.ward=v;}
    public String getBinType(){return binType;} public void setBinType(String v){this.binType=v;}
    public Integer getFillLevel(){return fillLevel;} public void setFillLevel(Integer v){this.fillLevel=v;}
    public String getStatus(){return status;} public void setStatus(String v){this.status=v;}
    public Double getLat(){return lat;} public void setLat(Double v){this.lat=v;}
    public Double getLng(){return lng;} public void setLng(Double v){this.lng=v;}
    public LocalDateTime getLastCollected(){return lastCollected;} public void setLastCollected(LocalDateTime v){this.lastCollected=v;}
    public LocalDateTime getLastUpdated(){return lastUpdated;} public void setLastUpdated(LocalDateTime v){this.lastUpdated=v;}
}
