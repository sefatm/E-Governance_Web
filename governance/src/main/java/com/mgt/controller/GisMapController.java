package com.mgt.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.service.GisMapService;

@RestController
@RequestMapping("/api/map")
public class GisMapController {

    @Autowired
    private GisMapService gisMapService;

    //Holding (Property) Map 
    @GetMapping("/holdings")
    public ResponseEntity<Map<String, Object>> getHoldingGeoData() {
        return ResponseEntity.ok(gisMapService.getHoldingGeoJSON());
    }

    @GetMapping("/holdings/ward/{wardNo}")
    public ResponseEntity<Map<String, Object>> getHoldingsByWard(@PathVariable int wardNo) {
        return ResponseEntity.ok(gisMapService.getHoldingsByWard(wardNo));
    }

    @GetMapping("/holdings/status/{status}")
    public ResponseEntity<Map<String, Object>> getHoldingsByTaxStatus(@PathVariable String status) {
        return ResponseEntity.ok(gisMapService.getHoldingsByTaxStatus(status));
    }

    //  Infrastructure Map 
    @GetMapping("/infrastructure/roads")
    public ResponseEntity<Map<String, Object>> getRoadGeoData() {
        return ResponseEntity.ok(gisMapService.getRoadGeoJSON());
    }

    @GetMapping("/infrastructure/drainage")
    public ResponseEntity<Map<String, Object>> getDrainageGeoData() {
        return ResponseEntity.ok(gisMapService.getDrainageGeoJSON());
    }

    @GetMapping("/infrastructure/street-lights")
    public ResponseEntity<Map<String, Object>> getStreetLightGeoData() {
        return ResponseEntity.ok(gisMapService.getStreetLightGeoJSON());
    }

    @GetMapping("/infrastructure/construction")
    public ResponseEntity<Map<String, Object>> getConstructionGeoData() {
        return ResponseEntity.ok(gisMapService.getConstructionGeoJSON());
    }

    @GetMapping("/infrastructure/all")
    public ResponseEntity<Map<String, Object>> getAllInfrastructureGeoData() {
        return ResponseEntity.ok(gisMapService.getAllInfrastructureGeoJSON());
    }

    @GetMapping("/complaints")
    public ResponseEntity<Map<String, Object>> getComplaintGeoData() {
        return ResponseEntity.ok(gisMapService.getComplaintGeoJSON());
    }

    //  Health & Waste Map
    @GetMapping("/health-centers")
    public ResponseEntity<Map<String, Object>> getHealthCenterGeoData() {
        return ResponseEntity.ok(gisMapService.getHealthCenterGeoJSON());
    }

    @GetMapping("/garbage-zones")
    public ResponseEntity<Map<String, Object>> getGarbageZoneGeoData() {
        return ResponseEntity.ok(gisMapService.getGarbageZoneGeoJSON());
    }

    @GetMapping("/waste-pickups")
    public ResponseEntity<Map<String, Object>> getWastePickupGeoData() {
        return ResponseEntity.ok(gisMapService.getWastePickupGeoJSON());
    }

    // Ward Boundaries 
    @GetMapping("/wards")
    public ResponseEntity<Map<String, Object>> getWardBoundaries() {
        return ResponseEntity.ok(gisMapService.getWardBoundaryGeoJSON());
    }
}
