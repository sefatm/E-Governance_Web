package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.PickupRequest;
import com.mgt.service.PickupRequestService;


@RestController
@RequestMapping(value = "/api/waste-request")
public class PickupRequestController {

    @Autowired 
    PickupRequestService wasteService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody PickupRequest waste) {
        try {
            wasteService.create(waste);
            return ResponseEntity.ok(Map.of("message", "Request submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<PickupRequest> getall() {
        return wasteService.getall();
    }

    @GetMapping("/phone/{phone}")
    public List<PickupRequest> byPhone(@PathVariable String phone) {
        return wasteService.findByPhone(phone);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        PickupRequest w = wasteService.getById(id);
        if (w == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(w);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        wasteService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody PickupRequest waste) {
        waste.setId(id);
        wasteService.update(waste);
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        wasteService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
