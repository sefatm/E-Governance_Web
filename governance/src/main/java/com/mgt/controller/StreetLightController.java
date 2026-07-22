package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.StreetLight;
import com.mgt.service.StreetLightService;

@RestController
@RequestMapping(value = "/api/street-light")
public class StreetLightController {

    @Autowired 
    StreetLightService lightService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody StreetLight light) {
        try {
            lightService.create(light);
            return ResponseEntity.ok(Map.of("message", "Application submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<StreetLight> getall() {
        return lightService.getall();
    }

    @GetMapping("/my-applications")
    public List<StreetLight> myApplications(@RequestParam String contact) {
        return lightService.findByContact(contact);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        lightService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/location/{id}")
    public ResponseEntity<Object> updateLocation(
            @PathVariable int id,
            @RequestBody Map<String, Double> body) {

        try {
            lightService.updateLocation(
                    id,
                    body.get("lat"),
                    body.get("lng"));

            return ResponseEntity.ok(
                    Map.of("message", "Location updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}