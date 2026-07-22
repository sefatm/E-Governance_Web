package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.GarbageSchedule;
import com.mgt.service.GarbageScheduleService;


@RestController
@RequestMapping(value = "/api/garbage-schedule")
public class GarbageScheduleController {

    @Autowired GarbageScheduleService garbageService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody GarbageSchedule garbage) {
        try {
            garbageService.create(garbage);
            return ResponseEntity.ok(Map.of("message", "Schedule created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<GarbageSchedule> getall() {
        return garbageService.getall();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        GarbageSchedule g = garbageService.getById(id);
        if (g == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(g);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        garbageService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody GarbageSchedule garbage) {
        garbage.setId(id);
        garbageService.update(garbage);
        return ResponseEntity.ok(Map.of("message", "Schedule updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        garbageService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
