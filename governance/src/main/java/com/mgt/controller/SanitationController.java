package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.Sanitation;
import com.mgt.service.SanitationService;

@RestController
@RequestMapping(value = "/api/sanitation")
public class SanitationController {

    @Autowired SanitationService sanitationService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Sanitation sanitation) {
        try {
            sanitationService.create(sanitation);
            return ResponseEntity.ok(Map.of("message", "Record added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<Sanitation> getall() {
        return sanitationService.getall();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        Sanitation s = sanitationService.getById(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        sanitationService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        sanitationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
