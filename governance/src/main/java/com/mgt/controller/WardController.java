package com.mgt.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mgt.model.Ward;
import com.mgt.service.WardService;

@RestController
@RequestMapping("/api/ward")
public class WardController {

    @Autowired WardService wardService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Ward ward) {
        try { return ResponseEntity.ok(wardService.create(ward)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @GetMapping("/getall")
    public List<Ward> getAll() { return wardService.getAll(); }

    @GetMapping("/getall-with-boundaries")
    public List<Ward> getAllWithBoundaries() { return wardService.getAllWithBoundaries(); }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        Ward w = wardService.getById(id);
        return w == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(w);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody Ward ward) {
        ward.setId(id);
        return ResponseEntity.ok(wardService.update(ward));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        wardService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        wardService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
