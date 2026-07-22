package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.WaterConnection;
import com.mgt.service.WaterConnectionService;

@RestController
@RequestMapping(value = "/api/water-connection")
public class WaterConnectionController {

    @Autowired 
    WaterConnectionService waterService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody WaterConnection water) {
        try {
            waterService.create(water);
            return ResponseEntity.ok(Map.of("message", "Application submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<WaterConnection> getall() {
        return waterService.getall();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        WaterConnection w = waterService.getById(id);
        if (w == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(w);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        waterService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody WaterConnection water) {
        water.setId(id);
        waterService.update(water);
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        waterService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
