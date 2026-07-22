package com.mgt.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="waste_collection_log")
public class WasteCollectionLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
 @Column(name="pickup_request_id") private Integer pickupRequestId;
 private String ward; private String area;
 @Column(name="waste_type") private String wasteType;
 @Column(name="estimated_weight_kg") private Double estimatedWeightKg;
 @Column(name="vehicle_no") private String vehicleNo;
 @Column(name="collector_name") private String collectorName;
 @Column(name="collection_date") private LocalDateTime collectionDate = LocalDateTime.now();
 @Column(name="completed_at") private LocalDateTime completedAt;
 private String status = "Completed";
 @Column(length=1000) private String remarks;
 public Integer getId(){return id;} public void setId(Integer v){id=v;}
 public Integer getPickupRequestId(){return pickupRequestId;} public void setPickupRequestId(Integer v){pickupRequestId=v;}
 public String getWard(){return ward;} public void setWard(String v){ward=v;}
 public String getArea(){return area;} public void setArea(String v){area=v;}
 public String getWasteType(){return wasteType;} public void setWasteType(String v){wasteType=v;}
 public Double getEstimatedWeightKg(){return estimatedWeightKg;} public void setEstimatedWeightKg(Double v){estimatedWeightKg=v;}
 public String getVehicleNo(){return vehicleNo;} public void setVehicleNo(String v){vehicleNo=v;}
 public String getCollectorName(){return collectorName;} public void setCollectorName(String v){collectorName=v;}
 public LocalDateTime getCollectionDate(){return collectionDate;} public void setCollectionDate(LocalDateTime v){collectionDate=v;}
 public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;}
}
