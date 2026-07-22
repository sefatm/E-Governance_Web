package com.mgt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.model.VotingCenter;
import com.mgt.model.VotingZone;
import com.mgt.service.ZoneCenterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ZoneCenterController {

	@Autowired
    private ZoneCenterService zoneCenterService;

    @GetMapping("/api/zone/getall")
    public ResponseEntity<Object> getZones() {
        return ResponseEntity.ok(zoneCenterService.getAllZones());
    }

    @GetMapping("/api/center/getall")
    public ResponseEntity<Object> getCenters() {
        return ResponseEntity.ok(zoneCenterService.getAllCenters());
    }

    @GetMapping("/api/center/by-zone/{zoneId}")
    public ResponseEntity<Object> getCentersByZone(@PathVariable Integer zoneId) {
        return ResponseEntity.ok(zoneCenterService.getCentersByZone(zoneId));
    }

    @PostMapping("/api/zone/create")
    public ResponseEntity<Object> createZone(@RequestBody VotingZone zone) {
        return ResponseEntity.ok(zoneCenterService.saveZone(zone));
    }

    @PostMapping("/api/center/create")
    public ResponseEntity<Object> createCenter(@RequestBody VotingCenter center) {
        return ResponseEntity.ok(zoneCenterService.saveCenter(center));
    }
}